#!/usr/bin/env bash

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd)"

cd $dir/../daemon
lein uberjar
mkdir -p ~/.inlein/daemons
find . -iname '*-standalone.jar' -exec cp {} ~/.inlein/daemons/ \;

cd $dir/../client
lein uberjar
mv target/inlein ~/bin/inlein

inlein --restart-daemon

cd $dir
