# Inlein

Run Clojure scripts with dependencies, but without classpath pains.

## Installation

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

## Usage

Inlein is usually called with the file to run, along with the parameters passed
to the script itself. On *nix-like systems, you can omit calling inlein directly
by making the script executable and prepending the shebang-line

```clj
#!/usr/bin/env inlein
```

at the very start of the script.

### Sample Script

As an example, let's make a script which calculates the *nth* prime number for
the user (0-indexed). We will use
[my prime library](https://github.com/hyPiRion/primes) to find the prime, to
show off dependency usage.

Type this into a file, and call it `primes.clj`:

```clj
#!/usr/bin/env inlein

'{:dependencies [[org.clojure/clojure "1.8.0"]
                 [com.hypirion/primes "0.2.1"]]
  :jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]}

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
contain JVM options you want to set, to speed up program startup or longer
running programs.

## License

Copyright © 2016 Jean Niklas L'orange

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version. See the file LICENSE.
