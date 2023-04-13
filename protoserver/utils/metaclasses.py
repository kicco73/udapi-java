#!/usr/bin/env python
# -*- coding: utf-8 -*-

from threading import Lock
import atexit

class Singleton(type):
	_lock = Lock()
	_instances = {}

	def __call__(cls, *args, **kwargs):
		with cls._lock:
			if cls not in cls._instances:
				if not cls._instances:
					atexit.register(cls.destroy_all)
				instance = super(Singleton, cls).__call__(*args, **kwargs)
				cls._instances[cls] = instance
			return cls._instances[cls]

	@classmethod
	def destroy_all(cls):
		with cls._lock:
			cls._instances = {}
