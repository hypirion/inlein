:;exec java "-Xbootclasspath/a:$0" "-Dinlein.client.file=$0" inlein.client.Main "$@"
@java -Xbootclasspath/a:"%~f0" -Dinlein.client.file="%~f0" inlein.client.Main %*
@goto :eof
