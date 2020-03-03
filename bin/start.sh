#!/bin/bash
export CLASSPATH=./classes:./lib/*
java -Xmx2048m -Xms2048m com.eqxiu.logserver.LogServer
