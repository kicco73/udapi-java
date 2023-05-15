#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess, select
import os, logging, json

from engine.operation import Operation

class BaseOperation(Operation):
	debug = False

	dev_bin = [
		'/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
		'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', 
		"@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_747zl4waewrm1vf9nuzgcr49f.argfile", 
		'cnr.ilc.Main',
	]

	prod_bin = [
		'/usr/bin/env', 'java', '-jar', '/app/rut.jar', 
	]

	args = [
		'--namespace', 'http://txt2rdf/test#',
		'--creator', 'bot',
		'--graphdb-url', 'http://localhost:7200',
		'--output-dir', os.path.join(os.getcwd(), 'resources')
	]

	def __init__(self, input='', resource_dir=None):
		super().__init__()
		self.input = input
		self.resource_dir = resource_dir

	def process_stderr(self, stderr):
		message = stderr.readline()
		if len(message) < 2: return len(message)
		try:
			dictionary = json.loads(message)					
		except:
			print("cannot parse json: %s", message)
			print(stderr.read())
			return
		if dictionary['event'] == 'notification':
			self.logger.log(level=logging.WARNING, msg=dictionary['detail'])
		elif dictionary['event'] == 'job update':
			self.logger.log(level=logging.WARNING, msg=dictionary['progress'])
		return len(message)

	def run_java(self, *args) -> str:
		output = ''
		final_args = self.dev_bin if self.debug else self.prod_bin
		final_args = final_args + self.args + list(args)
		with subprocess.Popen(final_args, stdin=subprocess.PIPE, stderr=subprocess.PIPE, stdout=subprocess.PIPE, text=True) as process:

			process.stdin.write(self.input)
			process.stdin.close()
			while process.poll() is None:
				r, _, _ = select.select([process.stderr, process.stdout], [], [], 0.5)
				
				if process.stdout in r:
					output += process.stdout.readline()

				if process.stderr in r:
					self.process_stderr(process.stderr)
					
			while True:
				if self.process_stderr(process.stderr) == 0:
					break

			output += process.stdout.read()

			if process.returncode:
				self.logger.log(level=logging.ERROR, msg='Error code %s - %s' % (
					process.returncode,
					' '.join(final_args)
				))

			return output

	def __str__(self):
		return f'{self.__class__.__name__}'