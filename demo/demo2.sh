#!/bin/bash

export PATH=../bin:$PATH

mkdir -p lib
../gradlew -p .. build
javac -d . -cp ../build/libs/*.jar *.java
