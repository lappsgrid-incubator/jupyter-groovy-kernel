#!/bin/bash
set -e
VERSION=`cat VERSION`
mvn clean package

DIST=jupyter-lsd-kernel
OLD=`find target -name "*jar-with-dependencies.jar"` 
NEW=`echo $OLD | sed 's/-jar-with-dependencies//'`
NEW=`basename $NEW`

#echo "Copying kernel to /usr/local/share/jupyter."
echo "Installing kernel to /Users/suderman/kernels"
cp $OLD /Users/suderman/kernels/$NEW

echo "Copying kernel to the Jupyter docker image."
cp $OLD /Users/suderman/Workspaces/docker/jupyter-notebook/lsd/$NEW

echo "Copying the kernel to the dist directory."
if [ ! -e $DIST ] ; then
	mkdir $DIST
fi
cp $OLD ./$DIST/$NEW

echo "Creating tgz file."
tar czf $DIST-$VERSION.tgz $DIST

echo "Done."
