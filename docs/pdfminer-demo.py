#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import re
import sys
import codecs
import getopt
import pdfminer
from cStringIO import StringIO
from pdfminer.pdfparser import PDFParser
from pdfminer.pdfinterp import PDFResourceManager,PDFPageInterpreter
from pdfminer.pdfdevice import PDFDevice, TagExtractor
from pdfminer.converter import XMLConverter, HTMLConverter, TextConverter
from pdfminer.cmapdb import CMapDB
from pdfminer.layout import LAParams
from pdfminer.converter import PDFPageAggregator

def convert_pdf(path):
	rsrcmgr = PDFResourceManager()
	retstr = StringIO()
	laparams = LAParams()
	device = TextConverter(rsrcmgr, retstr, codec='utf-8', laparams=laparams)
	fp = file(path, 'rb')
	process_pdf(rsrcmgr, device, fp)
	fp.close()
	device.close()
	str = retstr.getvalue()
	retstr.close()
	return str

def convert_pdf(path, outtype='txt', opts={}):
	outfile = path[:-3] + outtype
	outdir = '/'.join(path.split('/')[:-1])
	# debug option
	debug = 1
	# input option
	password = ''
	pagenos = set()
	maxpages = 0
	outdir = None
	layoutmode = 'normal'
	codec = 'utf-8'
	pageno = 1
	scale = 1
	showpageno = True
	laparams = LAParams()
	for (k, v) in opts:
	    if k == '-d': debug += 1
	    elif k == '-p': pagenos.update( int(x)-1 for x in v.split(',') )
	    elif k == '-m': maxpages = int(v)
	    elif k == '-P': password = v
	    elif k == '-o': outfile = v
	    elif k == '-n': laparams = None
	    elif k == '-A': laparams.all_texts = True
	    elif k == '-V': laparams.detect_vertical = True
	    elif k == '-M': laparams.char_margin = float(v)
	    elif k == '-L': laparams.line_margin = float(v)
	    elif k == '-W': laparams.word_margin = float(v)
	    elif k == '-F': laparams.boxes_flow = float(v)
	    elif k == '-Y': layoutmode = v
	    elif k == '-O': outdir = v
	    elif k == '-t': outtype = v
	    elif k == '-c': codec = v
	    elif k == '-s': scale = float(v)
	#
	PDFDocument.debug = debug
	PDFParser.debug = debug
	CMapDB.debug = debug
	PDFResourceManager.debug = debug
	PDFPageInterpreter.debug = debug
	PDFDevice.debug = debug
	#
	rsrcmgr = PDFResourceManager()
	outtype = 'text'
	if outfile:
	    outfp = file(outfile, 'w')
	else:
	    outfp = sys.stdout
	device = TextConverter(rsrcmgr, outfp, codec=codec, laparams=laparams)
	fp = file(path, 'rb')
	process_pdf(rsrcmgr, device, fp, pagenos, maxpages=maxpages,
		password=password, check_extractable=True)
	fp.close()
	device.close()
	outfp.close()

if __name__ == '__main__':
	fp = open('whwx-v2.pdf', 'rb')
	parser = PDFParser(fp)
	doc = PDFDocument()
	rsrcmgr = PDFResourceManager()
	device = PDFPageAggregator(rsrcmgr, laparams=LAParams())
	interpreter = PDFPageInterpreter(rsrcmgr, device)
	parser.set_document(doc)
	doc.set_parser(parser)

	content = []
	for index_page, page in enumerate(doc.get_pages()):
		interpreter.process_page(page)
		layout = device.get_result()
		prev_y1 = -1
		for index_hbox, hbox in enumerate(layout):
			if isinstance(hbox, pdfminer.layout.LTTextBoxHorizontal):
				# (0,0) => bottom left corner; +ve = right+up
				(x0, y0, x1, y1)=hbox.bbox	# bottom-left top-right
				text = hbox.get_text().strip().replace("\n","")
				text = re.sub(r'\s{2,}',' ',text)
				if isinstance(text, unicode) and text and \
					not re.match('^[0-9]{1,2}$', text):
					if u'\u7b2c' in text and u'\u7ae0' in text:
						assert index_hbox == 0,\
							'Index: '+str(index_hbox)+\
							' Page: '+str(index_page+1)+\
							' Left-indent: '+str(x0)+' '+text.encode('utf-8')
						print>>sys.stderr,'[title]',text.encode('utf-8')
						content.append(('title',text))
					else:
						if x0 in [63.59995]:
							if prev_y1 == -1:
								print>>sys.stderr,'[para]',text.encode('utf-8')
								content.append(('para',text))
							else:
								if abs(prev_y1 - y1) > 45:
									print>>sys.stderr,'[para]',text.encode('utf-8')
									content.append(('para',text))
								else:								
									print>>sys.stderr,'[body]',text.encode('utf-8')
									content.append(('body',text))
							prev_y1 = y1
						elif x0 in [117.6,99.59995,90.95005,85.9]:
							print>>sys.stderr,'[quote]',text.encode('utf-8')
							content.append(('quote',text))
						else:
							assert False, 'Page: '+str(index_page+1)+\
								' Left-indent: '+str(x0)+' '+\
								text.encode('utf-8')

	output_file = sys.stdout #codecs.open('the-reason-why.sql','w','utf-8')
	print>>output_file, "DELETE FROM toc;"
	print>>output_file, "DELETE FROM book;"
	chapter, verse = 0,1
	prev_body = ""
	for (doc_type, text) in content:
		if doc_type == 'title':
			# assume title is enclosed in <b></b> later
			print>>output_file, "INSERT INTO toc(title) VALUES('"+\
				text+"');"
			chapter, verse = chapter + 1, 1
			prev_body = ""
		elif doc_type == 'para':
			if prev_body:
				print>>output_file, "INSERT INTO book"+\
					"(chapter,verse,content) "+\
					"VALUES("+str(chapter)+","+\
					str(verse)+",'"+prev_body+"');"
				verse = verse + 1
			prev_body = text
		elif doc_type == 'body':
			prev_body = prev_body + text
		elif doc_type == 'quote':
			if prev_body:
				print>>output_file, "INSERT INTO book"+\
					"(chapter,verse,content) "+\
					"VALUES("+str(chapter)+","+\
					str(verse)+",'"+prev_body+"');"
				verse = verse + 1
				prev_body = ""
			print>>output_file, "INSERT INTO book"+\
				"(chapter,verse,content) "+\
				"VALUES("+str(chapter)+","+\
				str(verse)+",'<i>"+text+"</i>');"
			verse = verse + 1
	if prev_body:
		print>>output_file, "INSERT INTO book"+\
			"(chapter,verse,content) "+\
			"VALUES("+str(chapter)+","+\
			str(verse)+",'"+prev_body+"');"
		verse = verse + 1
	print>>output_file, "VACUUM;"
	output_file.close()
