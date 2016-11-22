#!/usr/bin/env bash

if [ -z "$PGP_PASSPHRASE" ] ; then
	source ~/.passphrases
fi

mvn clean
mvn -Dgpg.passphrase="$PGP_PASSPHRASE" package source:jar deploy

if [ "$1" = "-s" -o "$1" = "--site" ] ; then
	mvn site
fi
