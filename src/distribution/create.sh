#!/usr/bin/env bash

TARGET=../../target
NAME=jupyter-groovy-kernel
VERSION=`cat ../../VERSION`
JAR=$NAME-$VERSION.jar

DIST=$TARGET/$NAME-$VERSION

if [ ! -e $DIST ] ; then
    mkdir $DIST
fi

cp $TARGET/$JAR $DIST
cp kernel.json $DIST
echo "#!/bin/bash
set -e

if [ -z \$1 ] ; then
    echo 'No target directory specified.'
    echo 'Usage: ./install.sh <kernel directory>'
    echo
    echo 'Where <kernel directory> is the location the jar file'
    echo 'will be installed to and must already exist.'
    exit 1
fi
set -u

mkdir $NAME
cat kernel.json | sed \"s|__PATH__|\$1/$JAR|\" > $NAME/kernel.json
cp $JAR \$1
jupyter kernelspec install --replace --name groovy $NAME
rm -rf $NAME" > $DIST/install.sh
chmod ug+x $DIST/install.sh

cd $TARGET
echo "Creating tgz file."
tar czf $NAME-$VERSION.tgz $NAME-$VERSION
echo "Uploading $NAME-$VERSION.tgz"
scp -P 22022 $NAME-$VERSION.tgz suderman@anc.org:/usr/share/wp-lapplanders/downloads
echo "Uploading $NAME-latest.tgz"
scp -P 22022 $NAME-$VERSION.tgz suderman@anc.org:/usr/share/wp-lapplanders/downloads/$NAME-latest.tgz

echo "Done"
