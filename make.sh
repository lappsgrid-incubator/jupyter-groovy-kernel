#!/bin/bash
set -e
mvn package
cp target/ipython-groovy-kernel-1.0.0-SNAPSHOT.jar ~/.ipython/kernels/groovy/