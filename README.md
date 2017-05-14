# Inlein

Run Clojure scripts with dependencies, but without classpath pains.

## Installation

For a full introduction and tutorial to Inlein, please see the
[Getting Started](https://github.com/hyPiRion/inlein/wiki/Getting-Started)-page
on the wiki. If you know how to use and install Leiningen, you can follow the
installation steps and quickstarts in this README.

Inlein installs itself on the first run of the `inlein` script; there is no
separate install script. Follow these instructions to install Inlein
manually:

1. Make sure you have a Java JDK **version 7** or later.
2. [Download the latest `inlein` program released](https://github.com/hyPiRion/inlein/releases/latest)
3. Place it on your `$PATH`. (`~/bin` is a good choice if it is on your path.)
4. Set it to be executable. (`chmod 755 ~/bin/inlein`)
5. Run it.

Note that the first invokation with a script may be a bit slow, as inlein has to
retrieve and start the inlein daemon. You can force download and daemon startup
by calling `inlein --start-daemon`, to ensure faster startup times on subsequent
runs.

## Quickstart

Inlein is usually called with the file to run, along with the parameters passed
to the script itself.

```shell
inlein myscript.clj my args here
```

On *nix-like systems, you can omit calling inlein directly by making the script
executable and prepending the shebang-line

```clj
#!/usr/bin/env inlein
```

at the very start of the script.

### Sample Script

As an example, let's make a script which calculates the *nth* prime number for
the user (0-indexed). We will use
[my prime library](https://github.com/hyPiRion/primes) to find the prime, to
show how to add dependencies.

Type this into a file, and call it `primes.clj`:

```clj
#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [com.hypirion/primes "0.2.1"]]}

(require '[com.hypirion.primes :as p])

(when-not (first *command-line-args*)
  (println "Usage:" (System/getProperty "$0") "prime-number")
  (System/exit 1))

(-> (first *command-line-args*)
    (Long/parseLong)
    (p/get)
    println)
```

Now make it executable (`chmod +x primes.clj`), and try it out by e.g. calling
`./primes.clj 10`.

An inlein script begins with a quoted map, which contains the script's
dependencies. They are specified exactly like in Leiningen, as a vector of
dependencies associated with the `:dependency` key. The quoted map may also
contain JVM options you want to set (generally to speed up program startup).

Inlein also tags the name of the file to the System property `$0`, to ease
creation of "shell"-like scripts.

## Building from Source

If you have `~/bin` on your path and want to run a snapshot version of inlein,
you can just call `test/install.sh`. This should place the client in
`~/bin/inlein` and the proper deamon where it needs to be placed. Remember that
you have to override the installed inlein version manually if you want to
upgrade or downgrade again.

To play around with the source, or install manually, see the Bootstrapping
section in
[CONTRIBUTING.md](https://github.com/hyPiRion/inlein/blob/master/CONTRIBUTING.md#bootstrapping).

## License

Copyright © 2016-2017 Jean Niklas L'orange

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version. See the file LICENSE.
