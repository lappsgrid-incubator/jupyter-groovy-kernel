#!/bin/bash
set -e

VERSION=__VERSION__

user=

while [ -n $1 ] ; do
    case $1 in
        -u|--user)
            user=--user
            ;;
        *)
            KERNEL_DIR=$1
            ;;
    esac
    shift
done

if [ -z $KERNEL_DIR ] ; then
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

JAR=jupyter-groovy-kernel-$VERSION.jar

echo "Installing the Groovy kernel to $KERNEL_DIR"
cp $JAR $KERNEL_DIR
cat kernel.json | sed "s|__PATH__|$KERNEL_DIR/$JAR|" > $KERNEL_DIR/kernel.json

jupyter kernelspec install --replace $user --name groovy $KERNEL_DIR

echo "Done."
