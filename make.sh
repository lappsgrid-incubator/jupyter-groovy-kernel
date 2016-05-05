#!/bin/bash
set -e
VERSION=`cat VERSION`
mvn clean package

echo "Copying kernel to ~/.ipython/kernels and the Jupyter docker image."

OLD=`find target -name "*jar-with-dependencies.jar"` 
NEW=`echo $OLD | sed 's/-jar-with-dependencies//'`
NEW=`basename $NEW`

cp $OLD ~/.ipython/kernels/groovy/$NEW
cp $OLD /Users/suderman/Workspaces/docker/jupyter-notebook/groovy/$NEW
echo "Done."
