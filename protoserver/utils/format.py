#!/usr/bin/env python
# -*- coding: utf-8 -*-

from datetime import timedelta

def pretty_timedelta(dt: timedelta):

	days, seconds = dt.days, dt.seconds
	years, days = divmod(days, 365)
	hours, seconds = divmod(seconds, 3600)
	minutes, seconds = divmod(seconds, 60)
	
	output = []
	
	if years: output += [f"{years} {'years' if years != 1 else 'year'}"]
	if days: output += [f"{days} {'days' if days != 1 else 'day'}"]
	if hours: output += [f'{hours}h']
	if minutes: output += [f'{minutes}m']
	if seconds: output += [f'{seconds}s']

	return ' '.join(output)

digits4 = lambda data, unit='': f'{data:.4f} {unit}' if data else 'n/a'
digits2 = lambda data, unit='': f'{data:.2f} {unit}' if data else 'n/a'
