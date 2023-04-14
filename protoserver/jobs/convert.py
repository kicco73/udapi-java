#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess

from engine.operation import Operation

class Convert(Operation):
	def __init__(self, infilename: str, outfilename: str):
		super().__init__()
		self.infilename = infilename
		self.outfilename = outfilename

	def execute(self) -> str:
		args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', "@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_ccox37emu87o6ww09penhgt93.argfile", 'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--creator', 'kicco',
			'--tbx',
			'--', self.infilename
		]

		with open(self.infilename) as input:
			with open(self.outfilename, mode='w') as output:
				process = subprocess.run(args, stdin=input, stdout=output, stderr=subprocess.PIPE)
		return process.stderr.decode('utf-8')

	def __str__(self):
		return f'{self.__class__.__name__}'