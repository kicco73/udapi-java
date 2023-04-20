#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess
import os

from engine.operation import Operation

class BaseOperation(Operation):
	args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', 
			"@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_747zl4waewrm1vf9nuzgcr49f.argfile", 
			'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--creator', 'kicco',
			'--graphdb-url', 'http://localhost:7200',
			'--output-dir', os.path.join(os.getcwd(), 'resources')
		]

	def __init__(self, input='', resource_dir=None):
		super().__init__()
		self.input = input
		self.resource_dir = resource_dir

	def run_java(self, *args) -> str:
		process = subprocess.run(self.args + list(args), input=self.input.encode('utf-8'), stdout=subprocess.PIPE)
		return process.stdout.decode('utf-8')

	def __str__(self):
		return f'{self.__class__.__name__}'