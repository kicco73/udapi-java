#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess, select
import os, logging, json

from engine.operation import Operation

class BaseOperation(Operation):
	args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', 
			"@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_747zl4waewrm1vf9nuzgcr49f.argfile", 
			'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--creator', 'bot',
			'--graphdb-url', 'http://localhost:7200',
			'--output-dir', os.path.join(os.getcwd(), 'resources')
		]

	@property
	def progress(self):
		return 100 * self.__progress // self.__steps
	
	def __init__(self, input='', resource_dir=None):
		super().__init__()
		self.input = input
		self.resource_dir = resource_dir

	def run_java(self, *args) -> str:
		with subprocess.Popen(self.args + list(args), stdin=subprocess.PIPE, bufsize=1, stderr=subprocess.PIPE, stdout=subprocess.PIPE, text=True) as process:
			process.stdin.write(self.input)
			process.stdin.close()

			while process.poll() is None:
				message = process.stderr.readline()
				if len(message) < 2: continue
				dictionary = json.loads(message)
				if dictionary['event'] == 'notification':
					self.logger.log(level=logging.WARNING, msg=dictionary['detail'])
				elif dictionary['event'] == 'job update':
					self.logger.log(level=logging.WARNING, msg=dictionary['progress'])
					
			return process.stdout.read()

	def __str__(self):
		return f'{self.__class__.__name__}'