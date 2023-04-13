#!/usr/bin/env python
# -*- coding: utf-8 -*-

import smtplib
import logging
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from utils.decorators import setter

class MailConfig(object):
	def __init__(self, sender_address: str = 'bee@gofunq.com', host: str = 'locahost', port: int = 465, user: str = None, password: str = None):
		self.sender_address = sender_address
		self.smtp_host = host
		self.smtp_port = port
		self.smtp_username = user
		self.smtp_password = password

	def __str__(self):
		return f'sender: {self.sender_address}, SMTP: {self.smtp_host}:{self.smtp_port}'

class Mail(object):

	config = MailConfig()	

	def __init__(self):
		self.logger = logging.getLogger(self.__class__.__name__)
		self.message = MIMEMultipart()
		self.message['From'] = self.config.sender_address

	@setter
	def subject(self, subject):
		self.message['Subject'] = subject

	@setter
	def text(self, content):
		self.message.attach(MIMEText(content, 'plain'))	

	@setter
	def html(self, content):
		self.message.attach(MIMEText(content, 'html'))	

	def send(self, to: str):
		self.message['To'] = to
		text = self.message.as_string()
		session = smtplib.SMTP_SSL(self.config.smtp_host, self.config.smtp_port)
		try:
			session.login(self.config.smtp_username, self.config.smtp_password)
			session.sendmail(self.config.sender_address, to, text)
		except Exception as e:
			self.logger.error(e)
		finally:
			self.logger.info(f'Mail sent to: {to}')
			session.quit()

	def send_test(self):
		self.subject = 'Test mail from Python'
		self.text = 'How are you man?'
		self.send(to='enrico.carniani@gmail.com')

	