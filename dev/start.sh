#! /bin/bash -e -x

#     [ repl-port | web-port | cljs-build ]
clj -A:$1 -C:$1 -R:$1 -m ether.estuary.repl 6661 8080 $1