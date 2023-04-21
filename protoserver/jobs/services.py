#!/usr/bin/env python
# -*- coding: utf-8 -*-

from jobs.base import BaseOperation

class Analyse(BaseOperation):
	def execute(self) -> str:
		return self.run_java('--service', 'analyse', '--input-format', 'tbx')

class Filter(BaseOperation):
	def __init__(self, resource_dir: str, languages: list):
		super().__init__(resource_dir=resource_dir)
		self.languages = languages

	def execute(self) -> str:
		return self.run_java('--service', 'filter', 
		       '--filter-languages', ','.join(self.languages), 
		       '--', self.resource_dir) 

class Convert(BaseOperation):
	def execute(self) -> str:
		return self.run_java('--service', 'assemble', '--', self.resource_dir)

class Submit(BaseOperation):
	def __init__(self, resource_dir: str, repository: str):
		super().__init__(resouorce_dir=resource_dir)
		self.repository = repository

	def execute(self) -> str:
		return self.run_java('--service', 'submit', '--repository', self.repository, '--', self.resource_dir)
