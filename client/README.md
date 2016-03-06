# inlein client

This is the inlein client. The client is a program that calls the inlein daemon
(and starts/downloads it if it is not found), and interprets/runs the results
received from the daemon.

If you want to run a snapshot version of Inlein, you must also install the
Inlein daemon manually. See the `daemon` directory for more information on how
to do that. Keep in mind that this version won't be upgradable, so you have to
reinstall inlein if you want move back to a stable version.

## Developing

The easiest way to develop the inlein client is by running `lein run -- your
params here`. If you mainly work on the daemon, you may prefer faster client
startup speeds. Running `lein uberjar` will create `target/inlein`, which is a
fast executable snapshot version of the client.

## License

Copyright © 2016 Jean Niklas L'orange

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version. The licence can be found at the root of this project
(one directory up) named LICENSE.
