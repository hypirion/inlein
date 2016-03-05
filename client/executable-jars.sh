#!/bin/sh

if [ $# -ne 1 ]; then
    echo 'executable-jars.sh creates executable jars'
    echo 'Run it with `lein uberjar`'
    exit 1
fi

vsn="$1"
base=$(dirname $(readlink -f "$0"))
jarfile="$base/target/inlein-$vsn.jar"

if [ ! -f "$jarfile" ]; then
    echo "Could not find $jarfile,"
    echo 'which is needed to create executable jars'
    exit 1
fi

if [ ! -f "$base/dev-resources/prelude.sh" ] ; then
    echo 'Unable to find prelude.sh in dev-resources,'
    echo 'which is needed for executable jars'
    exit 1
fi

cat "$base/dev-resources/prelude.sh" "$jarfile" >> "$base/target/inlein"
chmod +x "$base/target/inlein"
echo "Created $base/target/inlein"
