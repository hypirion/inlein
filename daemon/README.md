# Inlein daemon

This is the inlein daemon. You don't tend to directly call this yourself, you
usually just end up letting the inlein client start it up. If it's not readily
available, it is downloaded and used.

However, if you want to use the inlein daemon snapshot version, you have to
manually install it. This can be done by doing `lein uberjar`, then copying the
standalone into `$HOME/.inlein/daemons`.

## Develop

To develop on the inlein daemon, you will first need to shutdown the running
daemon, if it exists. You can do so by calling `inlein --shutdown-daemon`. Then,
type `lein repl`, followed by `(go)` in the repl itself.

The daemon is a component app, and as such follows Stuart Sierra's
[reloaded workflow](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded):
Updating changes in the daemon is done by issuing `(reset)`. If you call
`--shutdown-daemon`, you can revive the system by issuing `(go)` again.

## License

Copyright © 2016 Jean Niklas L'orange

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version. The licence can be found at the root of this project
(one directory up) named LICENSE.
