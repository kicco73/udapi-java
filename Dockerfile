# ---- OpenJDK image
FROM openjdk:18-jdk-alpine AS base

# ---- Create app directory
WORKDIR /app

# ---- Upload the current directory and descendants to WORKDIR
# ---- Ignores files per .dockerignore
COPY ./protoserver .
COPY bin/libs/rut.jar rut.jar

# ---- Update OS
RUN apk --update upgrade

# ---- Install Alpine packages

RUN apk add \
  python3 \
  py-pip \
  bash

RUN pip install --ignore-installed distlib pipenv

# --- Set environment variables
ENV HOME /app
ENV LANG C

# ---- Install the application
RUN pipenv --python=/usr/bin/python3 install --deploy --system

# Start application
CMD python3 app.py