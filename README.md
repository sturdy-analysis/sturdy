# Compiling Apron

Install the [nix package manager](https://nixos.org/download.html):
```
# Linux
$ sh <(curl -L https://nixos.org/nix/install) --daemon

# MacOS
$ sh <(curl -L https://nixos.org/nix/install)
```

Build the apron library:
```
nix-build -E '(import <nixpkgs> {}).callPackage ./apron.nix {}'
```
The compiled library resides in directory `./result/lib/`.

Install the Scala build tool SBT and run the sturdy test suite to see if everything worked out:
```
sbt compile
sbt test
```
