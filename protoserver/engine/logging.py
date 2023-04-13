#!/usr/bin/env python
# -*- coding: utf-8 -*-

import logging

from pymitter import EventEmitter


class EmitLogger(logging.LoggerAdapter):

	notification = EventEmitter()

	def __init__(self, name: str):
		logger = logging.getLogger(name)
		super().__init__(logger, dict(title=name))

	def log(self, level, msg, *args, **kwargs):
		if level >= logging.WARNING:
			self.notification.emit('event', self.extra, level=logging.getLevelName(level), message=msg)
		return super().log(level, msg, *args, **kwargs)
