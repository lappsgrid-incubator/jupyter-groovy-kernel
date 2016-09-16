#!/bin/bash
set -eu

VERSION=`cat VERSION`
KERNEL_DIR=/usr/local/share/jupyter/kernels/groovy

# The name of the distribution directory to create.
NAME=jupyter-groovy-kernel
DIST=target/$NAME
JAR=target/$NAME-$VERSION.jar
NEW=kernel.jar

echo "Copying the kernel to the dist directory."
if [ ! -e $DIST ] ; then
	mkdir $DIST
fi
cp $JAR ./$DIST/$NEW
cp kernel.json ./$DIST
cp LICENSE ./$DIST
cp README.md ./$DIST

echo "Installing the Groovy kernel"
jupyter kernelspec install --name groovy --replace $DIST

#echo "Creating tgz file."
#cd target
#tar czf $NAME-$VERSION.tgz $NAME

echo "Done."
