#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

import time
import logging
import threading

from flask import render_template
from flask_socketio import SocketIO

from engine.engine import Engine
from engine.operation import OperationResult
from engine.logging import EmitLogger
from utils.metaclasses import Singleton

class Notifier(threading.Thread, metaclass=Singleton):
	def __init__(self, app, socketio: SocketIO):
		super().__init__()
		self.logger = logging.getLogger(self.__class__.__name__)
		self.socketio = socketio
		self.app = app

		@socketio.event
		def jobs(message):
			if not self.is_alive(): self.start()

		@EmitLogger.notification.on('event')
		def send_log(extra: dict, message: str, level: str):
			#self.logger.info(f'Broadcasting message: {message}')
			self.send_notification(level=level.lower(), color='orange', title=extra.get('title', level), message=message)

		@OperationResult.notification.on('aborted')
		def send_operation_aborted(operation_result: OperationResult, message: str):
			title = str(operation_result.operation)
			self.send_notification(level='error', color='red', title=title, message=message)

	def run(self):
		self.logger.warning('starting notification service')
		send_empty_updates = True
		while True:
			time.sleep(1.0)
			table = self.job_table_data()
			if table or send_empty_updates:
				self.send_jobs_update(table)
				send_empty_updates = not send_empty_updates or len(table) > 0

	def send_jobs_update(self, table):
		with self.app.app_context():
			text = render_template('jobs/socket_update.jinja', jobs=table) if table else ''
			self.socketio.emit('jobs update', text)

	def send_notification(self, title, message, level, color):
		with self.app.app_context():
			if isinstance(message, int):
				self.socketio.emit('job update', dict(progress=message, job=title))
			else:
				level = dict(warning='info', info='info', error='error')[level]
				self.socketio.emit('notification', dict(severity=level, summary=title, detail=message))

	def job_table_data(self):
		queues = Engine().queues()
		rows = []
		for name, jobs in queues.items():
			for job in jobs:
				percentage = job['progress']
				info = job['info']
				bar = f'<div style="position: relative"><span style="position: absolute; left: 50%; right: 50%">{percentage:.0f}%</span>'
				bar += f'<div style="display: flex"><span style="width: {percentage}%; background-color: lightblue; height: 15px;"></span>'
				bar += f'<span style="width: {100 - percentage}%; height: 15px; "></span></div>'
				bar += '</div>'
				rows += [[bar, info, name]]
				break

		return [['', '', 'Queue'], rows] if rows else []
