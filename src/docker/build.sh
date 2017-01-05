#!/usr/bin/env bash

# This script is simply used to ensure the most recent jar file and kernel.json file
# are copied into the working directory before running the docker build.

version=`cat ../../VERSION`
jar=jupyter-groovy-kernel-$version.jar
targetjar=../../target/$jar

if [ ! -e $targetjar ] ; then
    echo "JAR file not found. Please build the kernel before building the Docker container."
    exit 1
fi

if [ -e $jar ] ; then
    rm $jar
fi

if [ -e kernel.json ] ; then
    rm kernel.json
fi


cp ../../target/$jar .
