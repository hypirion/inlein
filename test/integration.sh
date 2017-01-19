#!/usr/bin/env bash

set -u

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd)"
script_dir="$dir/scripts"

export INLEIN_HOME=$(mktemp -d 2>/dev/null || mktemp -d -t 'inlein-home')
trap "rm -rf $INLEIN_HOME" EXIT

cd "$dir/../daemon"
lein uberjar
mkdir -p $INLEIN_HOME/daemons
find . -iname '*-standalone.jar' -exec cp -t $INLEIN_HOME/daemons {} \;

cd $dir/../client
lein uberjar
mv target/inlein $INLEIN_HOME/inlein
inlein=$INLEIN_HOME/inlein

PATH="$INLEIN_HOME:$PATH"
echo -n 2345 > $INLEIN_HOME/port
$inlein --start-daemon
trap "echo 'Shutting down temporary daemon...'; $inlein --shutdown-daemon; rm -rf $INLEIN_HOME" EXIT

err=0

cd "$script_dir"
echo 'Testing scripts...'

# Kinda hacky as we can't easily pass arrays to functions. We could perhaps read
# all the variables and assume the last one is the expected output.
testfn=()
function do_test {
    local out=''
    local start=''
    local stop=''
    echo "${testfn[@]}"
    start="$(date +%s%N)"
    out="$(${testfn[@]})"
    stop="$(date +%s%N)"
    printf "Time: %.3f seconds\n" "$(bc -l <<< "( $stop - $start ) / 1000000000.0")"
    if [ "$out" != "$1" ]; then
        err=1
        echo "ERROR: ${testfn[@]} failed"
        echo "  unexpected output:"
        echo
        echo "$out"
        echo
    fi
}

testfn=(./primes.clj)
do_test 'Usage: ./primes.clj prime-num'

testfn=(./primes.clj 100)
do_test '547'

testfn=(./cmd-name.clj)
do_test './cmd-name.clj'

testfn=(inlein cmd-name.clj)
do_test 'cmd-name.clj'

testfn=(inlein ./cmd-name.clj)
do_test './cmd-name.clj'

testfn=(./symlinked-cmd-name.clj)
do_test './symlinked-cmd-name.clj'

testfn=(./cli-args.clj)
do_test 'nil'

testfn=(./cli-args.clj a b c)
do_test '("a" "b" "c")'

exit $err
