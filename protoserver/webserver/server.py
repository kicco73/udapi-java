#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

import os

from flask import Flask
from flask_socketio import SocketIO
from flask_session import Session		
from flask_cors import CORS

import importlib
from werkzeug.serving import is_running_from_reloader

from webserver.controller import Controller
from webserver import controllers
from webserver import notifier

class WebServer(object):

	def __init__(self):
		app = self.app = Flask('GoFunQ Web Server')
		CORS(app)

		app.secret_key = 'super secret key'
		app.config['SESSION_TYPE'] = 'filesystem'
		app.config['RESOURCES_FOLDER'] = 'resources'

		os.makedirs(app.config['RESOURCES_FOLDER'], exist_ok=True)

		self.socketio = SocketIO(app)
		
		self.load_controllers(app, self.socketio, controllers)
		self.load_services(app, self.socketio)

	def load_controllers(self, app, socketio, module):
		for name in module.__all__:
			importlib.import_module(f'.{name}', module.__name__)

		for controller in Controller.__subclasses__():
			controller(app, socketio)

	def load_services(self, app, socketio):
		if not app.debug or is_running_from_reloader():
			self.notifier = notifier.Notifier(app, socketio)

	def run(self, debug=False, **kw):
		sess = Session()
		sess.init_app(self.app)
		self.socketio.run(self.app, debug=debug, allow_unsafe_werkzeug=True, **kw) #FIXME unsafe
