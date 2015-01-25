#!/usr/bin/env python
#-*- coding: utf-8 -*-
#@ http://denis.papathanasiou.org/?p=343

import os
from sys import stderr
from binascii import b2a_hex
from tempfile import mkdtemp
from pdfminer.pdfparser import PDFParser
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import PDFPageAggregator
from pdfminer.layout import LAParams, LTTextBox, LTTextLine, LTFigure, LTImage  # , LTChar
from Pamplemousse.preprocess.normalize import normalize

def _if_extractable(fn):
    '''Checks extractability PDFDocument object prior to
    applying Pamplemousse method.

    Use as decorator.
    '''
    def wrapped(self, *args):
        if not hasattr(self, 'pdf_doc'):
            raise TypeError('_if_extractable is wrapping a non-Pamplemousse object')

        result = None
        with PDFMinerWrapper(self.pdf_doc, self.passwd) as doc:  # self refers to the Pamplemousse object being passed
            if doc.is_extractable:
                result = fn(self, doc, *args)
        return result
    return wrapped


class PDFMinerWrapper(object):
    '''
    Usage:
    with PDFWrapper('/path/to/file.pdf') as doc:
        doc.dosomething()
    '''
    def __init__(self, pdf_doc, pdf_pwd=''):
        self.pdf_doc = pdf_doc
        self.pdf_pwd = pdf_pwd

    def __enter__(self):
        self.pdf = open(self.pdf_doc, 'rb')
        parser = PDFParser(self.pdf)  # create a parser object associated with the file object
        doc = PDFDocument()  # create a PDFDocument object that stores the document structure
        parser.set_document(doc)  # connect the parser and document objects
        doc.set_parser(parser)
        doc.initialize(self.pdf_pwd)  # pass '' if no password required
        return doc

    def __exit__(self, type, value, traceback):
        self.pdf.close()
        # if we have an error, catch it, log it, and return the info
        # if isinstance(value, Exception):
        #     # self.logError()
        #     print traceback
        #     return value


class Pamplemousse(object):
    def __init__(self, inputfile, passwd='', enc='utf-8'):
        '''
        inputfile : str
            Path to pdf file

        passwd : str
            Password to read pdf file.  Empty string if not password protected.

        enc: str
            Unicode encoding scheme
        '''
        self.pdf_doc = inputfile
        self.passwd = passwd
        self.enc = enc

        self.TOC = None  # Table of Contents

        self.images_folder = mkdtemp()
        self.truetext = False

    def __del__(self):
        # Clear temporary directory
        for fle in os.listdir(self.images_folder):
            os.remove(os.path.join(self.images_folder, fle))
        os.rmdir(self.images_folder)

        # Delete PDF file
        print "Debug Comment:  PDF has not been deleted"
        # os.remove(self.pdf_doc)  # NOTE:  uncomment in production code

    @_if_extractable
    def get_toc(self, doc):
        '''Return the table of contents (toc), if any, for this pdf file

        doc : PDFMinerWrapper
            PDFMinerWrapper object assigned to pdf file

        return: List
        '''
        toc = []
        try:
            toc = [(level, title) for level, title, dest, a, se in doc.get_outlines()]
        except PDFNoOutlines:
            pass
        return toc

    def write_file(self, folder, filename, filedata, flags='wt'):
        '''Write the file data to the folder and filename combination
        (flags: 'wt' for write text, 'wb' for write binary, use 'a' instead of 'w' for append)'''
        result = False
        if os.path.isdir(folder):
            with open(os.path.join(folder, filename), flags) as file_obj:
                file_obj.write(filedata)
                file_obj.close()
                result = True

        return result

    def determine_image_type(self, stream_first_4_bytes):  # TODO:  rewrite to use imghdr module
        '''Find out the image file type based on the magic number comparison of the first 4 (or 2) bytes'''
        file_type = None
        bytes_as_hex = b2a_hex(stream_first_4_bytes)
        if bytes_as_hex.startswith('ffd8'):
            file_type = '.jpeg'
        elif bytes_as_hex == '89504e47':
            file_type = '.png'
        elif bytes_as_hex == '47494638':
            file_type = '.gif'
        elif bytes_as_hex.startswith('424d'):
            file_type = '.bmp'
        elif bytes_as_hex == 'MM\x00\x2a' or bytes_as_hex == 'II\x2a\x00':
            file_type = '.tiff'

        return file_type

    def save_image(self, lt_image, page_number):
        '''Try to save the image data from this LTImage object, and return the file name, if successful'''
        result = None
        if lt_image.stream:
            file_stream = lt_image.stream.get_rawdata()
            if file_stream:
                file_ext = self.determine_image_type(file_stream[0:4])
                if file_ext:
                    file_name = ''.join([str(page_number), '_', lt_image.name, file_ext])
                    if self.write_file(self.images_folder, file_name, file_stream, flags='wb'):
                        result = file_name
        return result

    def to_bytestring(self, s):
        '''Convert the given unicode string to a bytestring, using the standard encoding,
        unless it's already a bytestring.

        s : unicode string
            string to convert to bytestring

        return:
            bytestring
        '''
        if s:
            if isinstance(s, str):
                return s
            else:
                return s.encode(self.enc)

    def update_page_text_hash(self, h, lt_obj, pct=0.2):
        '''Use the bbox x0,x1 values within percent, pct, to produce lists of associated text within the hash'''

        x0 = lt_obj.bbox[0]
        x1 = lt_obj.bbox[2]

        key_found = False
        for k, v in h.items():
            hash_x0 = k[0]
            if x0 >= (hash_x0 * (1.0 - pct)) and (hash_x0 * (1.0 + pct)) >= x0:
                hash_x1 = k[1]
                if x1 >= (hash_x1 * (1.0 - pct)) and (hash_x1 * (1.0 + pct)) >= x1:
                    # the text inside this LT* object was positioned at the same
                    # width as a prior series of text, so it belongs together
                    key_found = True
                    v.append(self.to_bytestring(lt_obj.get_text()))
                    h[k] = v
        if not key_found:
            # the text, based on width, is a new series,
            # so it gets its own series (entry in the hash)
            h[(x0, x1)] = [self.to_bytestring(lt_obj.get_text())]

        return h

    def parse_lt_objs(self, lt_objs, page_number, text=[]):
        '''Iterate through the list of LT* objects and capture the text or image data contained in each'''
        text_content = []

        page_text = {}  # k=(x0, x1) of the bbox, v=list of text strings within that bbox width (physical column)
        for lt_obj in lt_objs:
            if isinstance(lt_obj, LTTextBox) or isinstance(lt_obj, LTTextLine):
                # text, so arrange is logically based on its column width
                page_text = self.update_page_text_hash(page_text, lt_obj)
            elif isinstance(lt_obj, LTImage) and not self.truetext:  # only save images if they are presumed to contain text.
                if not self.save_image(lt_obj, page_number):
                    print >> stderr, "error saving image on page", page_number, lt_obj.__repr__
                    # consider logging errors to a file rather than printing errors to stderr
                    pass
            elif isinstance(lt_obj, LTFigure):
                # LTFigure objects are containers for other LT* objects, so recurse through the children-
                text_content.append(self.parse_lt_objs(lt_obj, page_number, text_content))

        for k, v in sorted([(key, value) for (key, value) in page_text.items()]):
            # sort the page_text hash by the keys (x0,x1 values of the bbox),
            # which produces a top-down, left-to-right sequence of related columns
            text_content.append(''.join(v))

        return '\n'.join(text_content)

    def _parse_pages(self, doc):
        '''With an open PDFDocument object, get the pages and parse each one

        doc : PDFDocument
            obtained via PDFMinerWrapper

        return:  List
            LT* objects
        '''
        rsrcmgr = PDFResourceManager()
        laparams = LAParams()
        device = PDFPageAggregator(rsrcmgr, laparams=laparams)
        interpreter = PDFPageInterpreter(rsrcmgr, device)

        # Get LTPage objects
        text_content = []
        for page in doc.get_pages():
            # receive the LTPage object for this page
            interpreter.process_page(page)
            text_content.append(device.get_result())  # LTPage object containing child LT* objects
                                                      # LTPage is iterable (but not indexable)

        # Determine if PDF contains text data or whether text is actually an embedded
        # image.
        self.truetext = self._detect_LTText(text_content)
        print self.truetext  # DEBUG

        # Parse LTPage and child objects.
        text_content = [self.parse_lt_objs(page, i + 1) for i, page in enumerate(text_content)]

        return text_content

    @_if_extractable
    def get_text(self, doc, norm='NFKD'):
        '''
        Return a generator for normalized unicode strings containing all text data on a page.

        norm : str
            Unicode normalization algorithm.  Default => 'NFKD'

        return:
            generator
            Normalized bytestrings of text data, excluding lines consisting only of u''.
        '''
        return (normalize(p.decode(self.enc), norm) for p in self._parse_pages(doc) if p != u'')

    def _detect_LTText(self, LTobjs, pct=.8):
        '''
        Heurisitcally assert if text is encoded in LTText* objects.

        The presence of an LTTextBox or LTTextLine on more than half of the LTPages is
            interpreted as proper text encoding (i.e.:  text is *not* an image).

        If the above test is inconclusive, the presence of large LTImage objects (defined as
            covering the proportion pct of the LTPage bounding box) in at least half of the
            LTPage objects is interpreted as text being stored in an image.

        LTFigure objects may contain aditional LT* objects, and are parsed one level deep
            in order to look for large nested images.  Parsing stops at one level to avoid
            problems of superposition.

        LTobjs : list
            list (or appropriate substitute iterable) containing all relevant LTPage objects
            in a document.

        pct : float
            Percent of LTPage bounding box to be occupied by an LTImage when conducting
            heuristic analsysis.

        returns:
            Boolean or NoneType
            Boolean asserts whether or not text is stored in LTText*.  Inconclusive results
                return a NoneType.  Note that by returning a NoneType, the evaluates in favor
                of text stored as an image by default.
        '''
        txt_in_pg = 0.
        ltimg_in_pg = 0.
        for page in LTobjs:
            plist = [obj for obj in page]
            i = 0
            while i < len(plist):
                lt_obj = plist[i]
                havetext = False
                if isinstance(lt_obj, LTTextBox) or isinstance(lt_obj, LTTextLine):
                    if not havetext:
                        txt_in_pg += 1.
                elif isinstance(lt_obj, LTImage):
                    if lt_obj.height * lt_obj.width >= pct:
                        ltimg_in_pg += 1.
                elif isinstance(lt_obj, LTFigure):
                    plist += [obj for obj in lt_obj if not isinstance(obj, LTFigure)]  # descend into LTFigure by one level max
                i += 1

            # for lt_obj in page:
            #     havetext = False
            #     if isinstance(lt_obj, LTTextBox) or isinstance(lt_obj, LTTextLine):
            #         if not havetext:
            #             txt_in_pg += 1.
            #     elif isinstance(lt_obj, LTImage):
            #         if lt_obj.height * lt_obj.width >= pct:
            #             ltimg_in_pg += 1.

        if txt_in_pg / len(LTobjs) >= .5:
            print "Text detected."  # DEBUG
            return True
        if ltimg_in_pg / len(LTobjs) >= .5:
            print "Large images detected."  # DEBUG
            return False

        return None

    def dump(self, data=None, fle=None):
        '''
        Save extracted text to a persistent file.  Useful for debugging.

        data : unicode string
            String of data to dump to file

        fle :  string
            file to which the data should be saved.  Default:  None  =>  save to
            Pamplemousse.images_folder tempdir using the contents of data as a filename.

        return:
            bool
            TRUE if pdf contained proper text data AND said text was successfully written to the file.
        '''
        if fle is None:
            fle = os.path.join(self.images_folder, 'Pamplemousse.txt')
        if data is None:
            data = (self.to_bytestring(s) for s in self.get_text())

        if self.truetext:  # Fail gracefully if text is stored as image
            with open(fle, 'wt') as f:
                f.writelines(data)
                success = True

        return success

# Room for Improvement
#  * Image Extraction - I'd like to be able to be at least as good as
#  pdftoimages, and save every file in ppm or pnm default format, but I'm
#  not sure what I could be doing differently

#  * Title and Heading Capitalization - this seems to be an issue with
#  PDFMiner, since I get similar results in using the command line tools,
#  but it is annoying to have to go back and fix all the mis-capitalizations
#  manually, particularly for larger documents.

#  * Title and Heading Fonts and Spacing - a related issue, though probably
#  something in my own code, is that those same title and paragraph headings
#  aren't distinguished from the rest of the text. In many cases, I have to
#  go back and add vertical spacing and font attributes for those manually.

#  * Page Number Removal - originally, I thought I could just use a regex
#  for an all-numeric value on a single physical line, but each document
#  does page numbering slightly differently, and it's very difficult to
#  get rid of these without manually proofreading each page.

#  * Footnotes - handling these where the note and the reference both appear
#  on the same page is hard enough, but doing it when they span different
#  (even consecutive) pages is worse.
