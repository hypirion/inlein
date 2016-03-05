# Contributing

Hi, thank you for your interest in contributing! We try to make it easy for new
contributors to help out, and do not hesitate to ask if there's anything you
wonder about. Contributors who have had a single patch accepted may request
commit rights.

Please report issues on the
[GitHub issue tracker](https://github.com/hyPiRion/inlein/issues). Sending bug
reports to personal email addresses is inappropriate. Simpler issues appropriate
for first-time contributors looking to help out are tagged "newbie", and should
have a some pointers on how to start.

Patches are preferred as GitHub pull requests. Please use topic branches when
sending pull requests rather than committing directly to master in order to
minimize unnecessary merge commit clutter. Direct pull requests towards the
master branch, not the stable branch.

## Bootstrapping

To build and develop inlein, you need Leiningen installed.

Inlein is split in two parts: The client and the daemon. The client is a Java
program which perform calls to the daemon, whereas the daemon is Clojure code
which reads and computes properties and the like.

To run the inlein daemon from source, you will first need to shutdown the
running daemon, if it exists. You can do so by calling `inlein
--shutdown-daemon`. Then, enter the `daemon` directory and type `lein repl`,
followed by `(go)` in the repl itself.

The daemon is a component app, and as such follows Stuart Sierra's
[reloaded workflow](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded):
Updating changes in the program is done by issuing `(reset)`. If you call
`--shutdown-daemon`, you can revive the system by issuing `(go)` again.

The easiest way to run the inlein client is by going into the `client` directory
and running `lein run -- your params here`. If you mainly work on the daemon,
you may prefer faster client startup speeds. Running `lein uberjar` will create
`target/inlein`, which is a fast executable snapshot version of the client.

## Codebase

Try to be aware of the conventions in the existing code, except the one where we
don't write tests. Make a reasonable attempt to avoid lines longer than 80
columns or function bodies longer than 20 lines. Don't use `when` unless it's
for side-effects.

## Tests

Before you're asking for a pull request, we would be very happy if you ensure
that the changes you've done doesn't break any of the existing test cases. While
there is a test suite, it's not terribly thorough, so don't put too much trust
in it. Patches which add test coverage for the functionality they change are
especially welcome.

(TODO: Standardise testing?)
