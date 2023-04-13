#!/usr/bin/env python
# -*- coding: utf-8 -*-

import functools
from functools import wraps
import logging
import inspect
import time

from numpy import isinf, isnan
from pandas import Series

def get_class_that_defined_method(meth):
    if isinstance(meth, functools.partial):
        return get_class_that_defined_method(meth.func)
    if inspect.ismethod(meth) or (inspect.isbuiltin(meth) and getattr(meth, '__self__', None) is not None and getattr(meth.__self__, '__class__', None)):
        for cls in inspect.getmro(meth.__self__.__class__):
            if meth.__name__ in cls.__dict__:
                return cls
        meth = getattr(meth, '__func__', meth)  # fallback to __qualname__ parsing
    if inspect.isfunction(meth):
        cls = getattr(inspect.getmodule(meth),
                      meth.__qualname__.split('.<locals>', 1)[0].rsplit('.', 1)[0],
                      None)
        if isinstance(cls, type):
            return cls
    return getattr(meth, '__objclass__', None)  

class setter(object):
    def __init__(self, func, doc=None):
        self.func = func
        self.__doc__ = doc if doc is not None else func.__doc__
    def __set__(self, obj, value):
        return self.func(obj, value)

def debug(func):

	@wraps(func)
	def wrapper(*args, **kwargs): 
		logger = logging.getLogger(func.__class__.__name__)
		logger.info(f'entering: {func.__name__}')
		result = func(*args, **kwargs)
		logger.info(f'exiting: {func.__name__}, result = {result}')
		return result

	return wrapper

def catch(func):

	def wrapper(*args, **kwargs): 
		try:
			return func(*args, **kwargs)
		except Exception as e:
			klass = get_class_that_defined_method(func)
			logger = logging.getLogger(klass.__name__)
			logger.error(f'{args[0].name}: {e}')

	return wrapper

def nan(func):
	def wrapper(*args, **kwargs): 
		result = func(*args, **kwargs)

		if isinstance(result, Series):
			if result.isnull().values.any():
				raise ValueError(f'{args[0].name}: series contains nan: {result}')
		elif result is None or isnan(result) or isinf(result):
			raise ValueError(f'{args[0].name}: cannot be {result}')
		return result
		
	return wrapper

def profile(func):
	@wraps(func)
	def wrapper(*args, **kwargs):
		logger = logging.getLogger(func.__class__.__name__)
		started_at = time.time()
		logger.info(f'{func.__name__}(): started profiling')
		result = func(*args, **kwargs)
		logging.info(f'{func.__name__}(): {time.time() - started_at:.2f}s')
		return result

	return wrapper

