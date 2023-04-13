#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys, os
import argparse
import configparser
import logging

from colorlog import ColoredFormatter

from webserver.server import WebServer

class App(object):

	LOGFORMAT = "  %(log_color)s%(levelname)-8s%(reset)s | %(name)-24s | %(log_color)s%(message)s%(reset)s"
	LOGLEVEL = logging.INFO

	def __init__(self):
		os.umask(0o002)
		logging.basicConfig(level=self.LOGLEVEL)
		formatter = ColoredFormatter(self.LOGFORMAT)
		stream = logging.StreamHandler()
		stream.setLevel(self.LOGLEVEL)
		stream.setFormatter(formatter)
		logging.root.handlers = []
		logging.root.addHandler(stream)
		self.logger = logging.getLogger(self.__class__.__name__)

		parser = argparse.ArgumentParser()
		parser.epilog = "tbx2rdf v1.0 - (c) 2023 CNR-ILC A. Zampolli"

		parser.add_argument('--config', default='config.ini', help='.ini configuration pathname')
		parser.add_argument(
			'--debug', action='store_true', help='enable debug mode')
	
		parser.set_defaults(func=self.webserver)

		self.parser = parser

	def add_config(self, args):
		args.config_parser = configparser.ConfigParser()
		args.config_parser.read(args.config)
		args.config = {section: dict(args.config_parser[section].items()) for section in args.config_parser.sections()}

	def run(self):
		args = self.parser.parse_args(sys.argv[1:])
		if 'func' in args:
			self.add_config(args)
			args.func(args)
		else:
			self.parser.print_help()

	def init(self):
		args = self.parser.parse_args(sys.argv[1:])
		self.add_config(args)

	def webserver(self, args):
		web_server = WebServer()

		kw = dict(
			debug=args.debug,
			host=args.config_parser.get('webserver', 'host') if args.config_parser.has_option('webserver', 'host') else '0.0.0.0', 
			port=args.config_parser.get('webserver', 'port') if args.config_parser.has_option('webserver', 'port') else 8000,
		)

		web_server.run(**kw)

if __name__ == '__main__':
	App().run()
else:
	App().init()
	