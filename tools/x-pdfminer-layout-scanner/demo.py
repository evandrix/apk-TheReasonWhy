#!/usr/bin/env python
#-*- coding: utf-8 -*-

import sys
import layout_scanner

filepath = "../../WHWXv2.pdf"

toc = layout_scanner.get_toc(filepath)
print toc

pages = layout_scanner.get_pages(filepath)
print pages[0]
