#!/usr/bin/env python
#-*- coding: utf-8 -*-

import scraperwiki
import requests

print scraperwiki.pdftoxml(open("../doc/whwx-v2.pdf").read()) #requests.get("http://calvarypandan.sg/WHWXv2.pdf").content)
