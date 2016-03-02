#!/usr/bin/env bash

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd)"

cd $dir/scripts

err=0

## TODO: Not entirely sure if a script is the best way to test this.

echo 'Testing scripts...'

if [ "$(./primes.clj)" != "Usage: ./primes.clj prime-num" ]; then
    err=1
    echo '"./primes.clj" failed'
fi

if [ "$(./primes.clj 100)" != "547" ]; then
    err=1
    echo '"./primes.clj 100" failed'
fi

exit $err
