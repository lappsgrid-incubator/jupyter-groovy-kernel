#!/bin/bash
set -e

if [ -z $1 ] ; then
    echo "Please specifiy an installation directory."
    exit 1
fi

set -u
KERNEL_DIR=$1

if [ ! -e $KERNEL_DIR ] ; then
    read -p "The directory $KERNEL_DIR does not exist. Woud you like to create it? [Y/n] " response
    if [ "$response" = "n" ] || [ "$response" = "N" ] ; then
        echo "Aborting."
        exit 1
    fi
    mkdir -p $KERNEL_DIR
fi

# The name of the distribution directory to create.
NAME=jupyter-groovy-kernel

VERSION=`cat VERSION`
JAR=$NAME-$VERSION.jar
DIST=target/groovy

if [ ! -e $DIST ] ; then
	mkdir -p $DIST
fi

cp target/$JAR $KERNEL_DIR
cat src/distribution/kernel.json | sed "s|__PATH__|$KERNEL_DIR/$JAR|" > $DIST/kernel.json

echo "Installing the Groovy kernel to $KERNEL_DIR"
if [ $(whoami) = root ]; then
  jupyter kernelspec install --replace --name groovy $DIST
else
  jupyter kernelspec install --replace --user --name groovy $DIST
fi

echo "Done."
