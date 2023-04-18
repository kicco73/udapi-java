#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess

from engine.operation import Operation

class Submit(Operation):
	def __init__(self, infilename: str, repository: str):
		super().__init__()
		self.infilename = infilename
		self.repository = repository

	def execute(self) -> str:
		args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', "@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_747zl4waewrm1vf9nuzgcr49f.argfile", 'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--creator', 'kicco',
			'--repository', self.repository,
			'--graphdb-url', 'http://localhost:7200',
			'--sparql',
			'--', self.infilename
		]

		process = subprocess.run(args, input='CLEAR DEFAULT'.encode('utf-8')) # FIXME: temporary hack
		process = subprocess.run(args, stderr=subprocess.PIPE)
		return process.stderr.decode('utf-8')

	def __str__(self):
		return f'{self.__class__.__name__}'