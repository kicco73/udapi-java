#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 

import logging

from apscheduler.schedulers.background import BackgroundScheduler

from engine.engine import Engine

class Scheduler(object):
	def __init__(self):
		self.scheduler = BackgroundScheduler()
		self.logger = logging.getLogger(self.__class__.__name__)
		self.start()
		self.scheduler.add_job(func=self.execute_hourly_maintainance, trigger="cron", hour="*", jitter=120)
		self.scheduler.add_job(func=self.execute_daily_maintainance, trigger="cron", hour=11, minute=20)
		self.scheduler.add_job(func=self.update_live_market, trigger="cron", minute='*/5')
		for minute in [2, 17, 32, 47]:
			self.scheduler.add_job(func=self.execute_trading, trigger="cron", minute=minute)

	def start(self):
		self.logger.warning('Starting scheduler service')
		self.scheduler.start()

	def execute_hourly_maintainance(self):
		#FIXME: 
		from tools.markets import update
		for operation in [update.UpdateAssetsOperation, update.UpdateCandlesAndPredictionsOperation]:
			Engine().enqueue(operation())

	def execute_daily_maintainance(self):
		#FIXME: 
		from tools.markets import update		
		from tools import cleanup

		for operation in [update.UpdateCandlesOperation, cleanup.CleanUpOperation]:
			Engine().enqueue(operation())

	def execute_trading(self):
		#FIXME: 
		from tools.investments import trade
		Engine().enqueue(trade.TradeAll())

	def update_live_market(self):
		#FIXME: 
		from tools.investments import prices
		Engine().enqueue(prices.UpdatePrices())

