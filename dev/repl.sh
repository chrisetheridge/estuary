#!/usr/bin/env bash
set -euo pipefail

clj -A:cljs:dev -m ether.repl 6661 7771 $1

