#!/usr/bin/env python

from docx import *

document = opendocx('input.docx')
print len(document.xpath('/w:document//w:t', namespaces=nsprefixes))
for elem in document.xpath('/w:document//w:t', namespaces=nsprefixes):
	print elem.text, elem.getparent().values(), elem.getparent().getparent().values(), elem.getparent().getparent().getparent()
docbody=document.xpath('/w:document/w:body', namespaces=nsprefixes)[0]
print map(lambda x:x.getchildren(),docbody.getchildren())