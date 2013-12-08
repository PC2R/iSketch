#!/bin/bash

if [ $# -eq 0 ]; then
	java -classpath ./class Client
fi

if [ $# -eq 2 ]; then
	java -classpath ./class Client "$1" "$2"
fi

if [ $# -eq 4 ]; then
	java -classpath ./class Client "$1" "$2" "$3" "$4"
fi
