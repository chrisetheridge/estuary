# Estuary

Music flows

## Quick start

- Install dependencies:

```bash
brew update
brew install clojure
brew install node
```

- Install node dependencies:

```bash
npm install
```

- Run dev repl

```bash
./dev/dev.sh 

# OR

clj -C:dev -R:dev -m nearby.repl
# repl will start on port 6661
# server will start on 8080
# cljs and css watcher will start

# -C and -R specify aliases for classpath and depdendency resolving
```