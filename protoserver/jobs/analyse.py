#!/usr/bin/env python
# -*- coding: utf-8 -*-

import subprocess

from engine.operation import Operation

class Analyse(Operation):
	def __init__(self, tbx: str):
		super().__init__()
		self.tbx = tbx

	def execute(self) -> str:
		args = ['/usr/bin/env', '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java',
   			'-cp', '/Users/enricocarniani/Documents/udapi-java/bin/main', "@/var/folders/vw/clt3dc494hg0bcmvk_1pkwdw0000gn/T/cp_ccox37emu87o6ww09penhgt93.argfile", 'cnr.ilc.Main',
			'--namespace', 'http://txt2rdf/test#',
			'--datetime', '2023-04-10T10:02+02:00',
			'--creator', 'kicco',
			'--tbx', '--json'
		]

		process = subprocess.run(args, input=self.tbx.encode('utf-8'), stdout=subprocess.PIPE)
		return process.stdout.decode('utf-8')

	def __str__(self):
		return f'{self.__class__.__name__}'