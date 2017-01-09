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
cat install-template.sh | sed "s/__VERSION__/$VERSION/" > $DIST/install.sh
chmod ug+x $DIST/install.sh

cd $TARGET
echo "Creating tgz file."
tar czf $NAME-$VERSION.tgz $NAME-$VERSION
echo "Uploading $NAME-$VERSION.tgz"
scp -P 22022 $NAME-$VERSION.tgz suderman@anc.org:/usr/share/wp-lapplanders/downloads
echo "Uploading $NAME-latest.tgz"
scp -P 22022 $NAME-$VERSION.tgz suderman@anc.org:/usr/share/wp-lapplanders/downloads/$NAME-latest.tgz

echo "Done"
