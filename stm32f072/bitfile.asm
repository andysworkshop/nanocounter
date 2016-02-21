/*
 * This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */

 	.global BitFileStart
	.global BitFileSize

BitFileStart:
	.incbin "../xc3s50/main.bit"
	BitFileSize=.-BitFileStart
