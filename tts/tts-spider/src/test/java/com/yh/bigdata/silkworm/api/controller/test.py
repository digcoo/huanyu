#!/usr/bin/python
#-*-coding:utf-8-*-
import sys
import time

while True:

    try:
        print("hello world") 
        time.sleep(3)
    except Exception as e:
        print(e) 
sys.exit(0)
