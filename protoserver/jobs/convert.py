#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess

from engine.operation import Operation

class Convert(Operation):
	def __init__(self, resourcedir: str):
		super().__init__()
		self.resourcedir = resourcedir

	def execute(self) -> str:
		args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', "@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_747zl4waewrm1vf9nuzgcr49f.argfile", 'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--creator', 'kicco',
			'--service', 'assemble',
			'--', self.resourcedir
		]

		process = subprocess.run(args, stdout=subprocess.PIPE)
		return process.stdout.decode('utf-8')

	def __str__(self):
		return f'{self.__class__.__name__}'