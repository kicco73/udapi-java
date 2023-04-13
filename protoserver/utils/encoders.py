#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

from datetime import datetime, timedelta
import json


class JSONEncoderWithDateTime(json.JSONEncoder):
	def default(self, obj):
		if isinstance(obj, datetime):
			return obj.isoformat()
		if isinstance(obj, timedelta):
			return obj.total_seconds()
		return json.JSONEncoder.default(self, obj)
