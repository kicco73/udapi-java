#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess, select
import os, logging, json

import time
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
	
	def __init__(self, input='', resource_dir=None):
		super().__init__()
		self.input = input
		self.resource_dir = resource_dir

	def process_stderr(self, message):
		if len(message) < 2: return
		try:
			dictionary = json.loads(message)					
		except:
			print("cannot parse json: %s", message)
			print(process.stderr.read())
			return
		if dictionary['event'] == 'notification':
			self.logger.log(level=logging.WARNING, msg=dictionary['detail'])
		elif dictionary['event'] == 'job update':
			self.logger.log(level=logging.WARNING, msg=dictionary['progress'])

	def run_java(self, *args) -> str:
		output = ''
		with subprocess.Popen(self.args + list(args), stdin=subprocess.PIPE, stderr=subprocess.PIPE, stdout=subprocess.PIPE, text=True) as process:
			process.stdin.write(self.input)
			process.stdin.close()
			while process.poll() is None:
				r, _, _ = select.select([process.stderr, process.stdout], [], [], 0.5)
				
				if process.stdout in r:
					output += process.stdout.readline()

				if process.stderr in r:
					message = process.stderr.readline()
					self.process_stderr(message)
					
			output += process.stdout.read()
			self.process_stderr(process.stderr.read())
			return output

	def __str__(self):
		return f'{self.__class__.__name__}'