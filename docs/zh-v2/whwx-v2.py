#!/usr/bin/env python3
#-*- coding: utf-8 -*-

from __future__ import print_function
import os
import sys
import codecs
import json
#import ftfy
import string
from pprint import pprint

if sys.stdout.encoding != 'UTF-8':
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
if sys.stderr.encoding != 'UTF-8':
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')

data = json.loads(codecs.open("whwx-v2.json", "r", "utf-8").read().strip())
#data = json.loads(open("whwx-v2.json", "r").read().strip())

from collections import Counter
def mode(lst):
    data = Counter(lst)
    return data.most_common(1)[0][0]

def is_ascii(s):
	return all(ord(c) < 128 for c in s)

chap, verse = 0, 0
titles, texts = [], []
for i, page in enumerate(data["page"]):
#	if i!=4: continue
#	print("--- i={0} ---".format(i), file=sys.stderr)
	leftmost, fw = 21 if i%2==0 else 43, 276
	ts = []
	for text in page["text"]:
		if "#text" not in text: continue
		# ignore lines consisting only of punctuations
		if len(''.join([c for c in text["#text"] if c not in set(string.punctuation)]).strip()) == 0: continue
		ts.append( (int(text["-top"]), int(text["-left"]),
			int(text["-width"]),
			#ftfy.fix_text(text["#text"])) )
			text["#text"]) )

	# merge consecutive part of lines which belong together
	# until width(current line) ~ full width
	for t, item in enumerate(ts):
		top, left, w, txt = item
		if left == leftmost and w < fw:
			nxt = t+1
			while nxt < len(ts):
				ntop, nleft, nw, ntxt = ts[nxt]
				if abs(ntop-top) > 3: break
				assert nleft > leftmost
				w += nw
				txt += ntxt
				del ts[nxt]
				if abs(w-fw) <= 3:
					w = fw
					break
				nxt += 1
		ts[t] = (top,left,w,txt)
	ts.sort()

	# separate title lines
	if ts[0][-1].startswith(u"第") and ts[0][-1].endswith(u"章"):
		title = ts[0][-1] + u" " + ts[1][-1]
		titles.append(title)
		chap += 1
		verse = 0
		del ts[:2]
	# delete footer number line
	if ts[-1][-1].isnumeric(): del ts[-1]

	# italic partially filled lines (assume poetry)
	for t, item in enumerate(ts):
		top, left, w, txt = item
		if left > leftmost and w != fw \
		and len(''.join([c for c in ts[t][-1] if not is_ascii(c)]).strip()) > 1:
			ts[t] = (top, left, w, "<i>" + ts[t][-1] + "</i>")
#	pprint(ts)

	# consoliate lines of the same paragraph
	# vertical spacing ~19-20, instead of ~39-40 for different paragraphs
	for t, item in enumerate(ts):
		top, left, w, txt = item
		if left == leftmost and w == fw:
			nxt, last_top = t+1, top
			while nxt < len(ts):
				ntop, nleft, nw, ntxt = ts[nxt]
				if ntop - last_top >= 35: break
				if is_ascii(txt[-1]) and is_ascii(ntxt[0]) \
				and ntxt[0] != " ":
					txt += " "
				txt += ntxt
				last_top = ntop
				del ts[nxt]
		ts[t] = (top,left,w,txt)

	# remove spaces between 2 consecutive chinese chars
	# but preserve whitespace between 2 ASCII chars
	for t, item in enumerate(ts):
		top, left, w, txt = item
		ntxt = txt[0]
		for nc, c in enumerate(txt):
			if 1 <= nc and nc <= len(txt)-2:
				if txt[nc] == " " and not is_ascii(txt[nc+1]):
					continue
				else:
					ntxt += txt[nc]
		ntxt += txt[nc]
		ts[t] = (top,left,w,ntxt)

	# construct SQL tuple
	for t in ts:
		top, left, w, text = t
		verse += 1
		texts.append( (chap, verse, text) )

#	print(u'\n'.join([t[-1] for t in ts]), file=sys.stderr)
#	print("-------", file=sys.stderr)

import sqlite3
filename = "the-reason-why.db"
if os.path.exists(filename): os.remove(filename)
conn = sqlite3.connect(filename)
#conn = sqlite3.connect(':memory:')
c = conn.cursor()
c.execute('''CREATE TABLE toc
	(id integer PRIMARY KEY, title text)''')
c.executemany("INSERT INTO toc VALUES (?,?)", [(i+1,t) for i,t in enumerate(titles)])
#for row in c.execute('SELECT * FROM toc'): print(row)
c.execute('''CREATE TABLE book
	(chapter integer, verse integer, content text)''')
c.executemany("INSERT INTO book VALUES (?,?,?)", texts)
#for row in c.execute('SELECT * FROM book'): print(row)
c.execute("VACUUM;")
conn.commit()
conn.close()
