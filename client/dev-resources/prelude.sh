#!/bin/sh

exec java "-Xbootclasspath/a:$0" "-Dinlein.client.file=$0" inlein.client.Main "$@"
