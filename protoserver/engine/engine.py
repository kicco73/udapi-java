#!/usr/bin/env python
# -*- coding: utf-8 -*-

import queue
import logging
import threading
import traceback

from engine.operation import Operation, OperationResult
from utils.metaclasses import Singleton

class SerialEngine(threading.Thread):

	def __init__(self):
		super().__init__()
		self.logger = logging.getLogger(self.__class__.__name__)
		self.lock = threading.Lock()
		self.__queue = queue.Queue(maxsize=100)
		self.__operations = []
		self.running = True
		self.start()

	def run(self):
		self.logger.info('Engine started')
		
		while self.running:
			operation, operation_result = self.__queue.get()
			if operation is None: break
			self.logger.info('Executing operation: %s', operation)
			try:
				result = operation.execute()
				self.logger.info('Operation finished: %s', operation)
				operation_result.completed(result)
			except Exception as e:
				message = e.message if hasattr(e, 'message') else str(e)
				self.logger.error(f'Operation aborted: {operation} - {message}')
				operation_result.aborted(message)
				print(traceback.format_exc())
			finally:
				with self.lock:
					self.__operations.remove(operation)

		self.logger.info('Engine finished queue')

	def enqueue(self, operation) -> OperationResult:
		assert isinstance(operation, Operation)
		self.logger.debug('Enqueuing operation')
		operation_result = OperationResult(operation)
		try:
			self.__queue.put((operation, operation_result), block=False)
			with self.lock:
				self.__operations.append(operation)
		except queue.Full:
			self.logger.error(f"Cannot enqueue: queue is full")
			raise
		except Exception as e:
			self.logger.error(e)
			raise

		return operation_result

	def jobs(self):
		with self.lock:
			return [{'info': str(op), 'progress': op.progress} for op in self.__operations]

	def already_queued(self, operation: Operation):
		with self.lock:
			for op in self.__operations:
				if op == operation:
					return True
		return False

	def shutdown(self):
		self.running = False
		self.__queue.put((None, None), block=False)

class Engine(object, metaclass=Singleton):
	def __init__(self):
		self.logger = logging.getLogger(self.__class__.__name__)
		self.lock = threading.Lock()
		self.serial_engines = {}

	def enqueue(self, operation: Operation, queue_name: str = 'Operation') -> OperationResult:
		with self.lock:
			engine : SerialEngine = self.serial_engines.get(queue_name)
			if engine is None:					
				self.logger.warning(f'creating {queue_name} serial engine')
				engine = SerialEngine()
				self.serial_engines[queue_name] = engine
			return engine.enqueue(operation)

	def queues(self):
		with self.lock:
			return {name: engine.jobs() for name, engine in self.serial_engines.items() if engine.jobs()}

	def already_queued(self, operation: Operation):
		with self.lock:
			for engine in self.serial_engines.values():
				if engine.already_queued(operation):
					return True
		return False

	def shutdown(self):
		self.logger.warning('shutdown(): waiting for engines to finish')
		with self.lock:
			for engine in self.serial_engines.values():
				engine.shutdown()
				engine.join(timeout=1.0)
		self.logger.warning('shutdown(): complete')

	def __del__(self):
		self.shutdown()
