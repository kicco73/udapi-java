#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

from functools import cached_property
import multiprocessing
from concurrent.futures import ProcessPoolExecutor
import logging

from utils.metaclasses import Singleton

class Pool(object, metaclass=Singleton):

	@property
	def logger(self):
		return logging.getLogger(self.__class__.__name__)

	@cached_property
	def executor(self):
		self.logger.warning('executor(): starting up multiprocessors')
		max_workers = multiprocessing.cpu_count() - 1
		executor = ProcessPoolExecutor(max_workers=max_workers)
		return executor

	def map(self, method, args):
		return self.executor.map(method, args)

	def submit(self, method, *args):
		return self.executor.submit(method, *args)

	def shutdown(self):
		self.logger.warning('shutdown(): waiting for processes to finish')
		self.executor.shutdown(wait=True)
		self.logger.warning('shutdown(): complete')

	def __del__(self):
		self.shutdown()