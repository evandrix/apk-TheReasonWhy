#!/usr/bin/env python
# -*- coding: utf-8 -*-

with open('the-reason-why.txt','r') as f:
	print "DELETE FROM toc;"
	titles = {}
	with open('toc.txt', 'r') as g:
		titles = map(lambda t:t.strip().replace("'", "''"), g.readlines())
	for title in titles:
		line = title.strip().split()
		id, title = line[0], ' '.join(line[1:])
		print "INSERT INTO toc(id,title) VALUES(%d,'%s');" %(int(id),title)

	print "DELETE FROM book;"
	chapter, verse = 1, 1
	page_no, bold_next_line, enable_italic, is_prayer = 4, False, False, False
	content = ""
	for idx, line in enumerate(f.readlines()):
		line = line.strip().replace("'", "''")
		if line in titles:
#			print 'title_line'
			bold_next_line = True
			continue
		elif bold_next_line:
			print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'<b>%s</b>');" %(chapter,verse,line)
			bold_next_line = False
			verse+=1
		elif line == str(page_no):
#			print 'page_break'
			if is_prayer:
				chapter+=1
				verse=1
				is_prayer = False
			enable_italic = False
			page_no+=1
		elif line.startswith('Thought:'):
#			print 'thought'
			enable_italic = True
			print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'<i>%s</i>');" %(chapter,verse,line)
			verse+=1
		elif line.startswith('Prayer:') and line != 'Prayer: "O God, I cannot understand the mystery of it all. I':
#			print 'prayer'
			enable_italic = True
			print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'<i>%s</i>');" %(chapter,verse,line)
			verse+=1
			is_prayer = True
		else:
#			print 'content'
			if enable_italic:
				# continuation of thought/prayer
				print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'<i>%s</i>');" %(chapter,verse,line)
				verse+=1
			else:
				# others, ie. normal paragraph content
				content = content.strip() + " " + line
				if "<i>" not in line and "<b>" not in line:
					if len(line) < 40:
						print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'%s');" %(chapter,verse,content)
						content = ""
						verse+=1
				else:
					print "INSERT INTO book(chapter,verse,content) VALUES(%d,%d,'%s');" %(chapter,verse,line)
					verse+=1
