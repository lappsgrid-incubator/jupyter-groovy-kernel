#!/bin/bash
set -e

if [ -z $1 ] ; then
    echo "Please specifiy an installation directory."
    exit 1
fi

set -u
KERNEL_DIR=$1

if [ ! -e $KERNEL_DIR ] ; then
    echo -n "The directory $KERNEL_DIR does not exist. Woud you like to create it? [Y/n] "
    read response
    if [ -z $response ] ; then response=y ; fi
    if [ $response = "n" ] || [ $response = "N" ] ; then
        echo "Aborting."
        exit 1
    fi
    #mkdir -p $KERNEL_DIR
fi
echo "Installing the kernel to $KERNEL_DIR"


# The name of the distribution directory to create.
NAME=jupyter-groovy-kernel

VERSION=`cat VERSION`
JAR=$NAME-$VERSION.jar
DIST=target/$NAME

echo "Copying the kernel to the dist directory."
if [ ! -e $DIST ] ; then
	mkdir -p $DIST
fi

cp target/$JAR $KERNEL_DIR
cat kernel.json | sed "s|__PATH__|$KERNEL_DIR/$JAR|" > ./$DIST/kernel.json

echo "Installing the Groovy kernel"
jupyter kernelspec install --replace --name groovy $DIST

echo "Done."
