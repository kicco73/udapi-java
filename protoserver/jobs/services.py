#!/usr/bin/env python
# -*- coding: utf-8 -*-

from jobs.base import BaseOperation

class Analyse(BaseOperation):
	def execute(self) -> str:
		return self.run_java('--service', 'analyse', '--input-format', 'tbx')

class Filter(BaseOperation):
	def execute(self) -> str:
		return self.run_java('--service', 'filter', '--', self.resource_dir) 

class Convert(BaseOperation):
	def execute(self) -> str:
		return self.run_java('--service', 'assemble', '--', self.resource_dir)

class Submit(BaseOperation):
	def __init__(self, resource_dir: str, repository: str):
		super().__init__(resouorce_dir=resource_dir)
		self.repository = repository

	def execute(self) -> str:
		return self.run_java('--repository', self.repository, '--service', 'submit', '--', self.resource_dir)
