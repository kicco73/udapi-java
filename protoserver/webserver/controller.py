#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

from engine.logging import EmitLogger

class Controller(object):

	def __init__(self, app, socketio):
		self.logger = EmitLogger(self.__class__.__name__)
		self.app = app
		self.socketio = socketio
