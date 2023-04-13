#!/usr/bin/env python
# -*- coding: utf-8 -*-

from abc import ABC, abstractmethod
import threading

from pymitter import EventEmitter
from engine.logging import EmitLogger

class Operation(ABC):

	def __init__(self):
		super().__init__()
		self.__steps = 1
		self.__progress = 0

	@property
	def logger(self):
		return EmitLogger(self.__class__.__name__)

	@property
	def progress(self):
		return 100 * self.__progress // self.__steps

	@abstractmethod
	def execute(self):
		pass

	def set_steps(self, steps):
		self.__steps = steps
		self.__progress = 0

	def next_step(self, message=''):
		self.__progress += 1
		self.logger.debug("Progress: %d/%d (%d%%) %s", self.__progress, self.__steps, self.progress, message)

	def __eq__(self, other):
		return isinstance(other, self.__class__)

	@abstractmethod
	def __str__(self):
		pass

class OperationResult(ABC):

	notification = EventEmitter()

	def __init__(self, operation: Operation):
		super().__init__()
		self.operation = operation
		self.__result = None
		self.__ready = threading.Event()

	@property
	def result(self):
		self.__ready.wait()
		if isinstance(self.__result, Exception): raise self.__result
		return self.__result

	def aborted(self, reason):
		self.__result = ValueError(reason)
		self.__ready.set()
		self.notification.emit('aborted', self, message = reason)

	def completed(self, result):
		self.__result = result
		self.__ready.set()
		self.notification.emit('completed', self)


