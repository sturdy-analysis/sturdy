You must build and install apron using the nix package manager, use this installer https://zero-to-nix.com/concepts/nix-installer/.
You must build and install apron using the nix package manager (https://nixos.org/download/).
On Mac use the Determinate Nix installer (https://zero-to-nix.com/concepts/nix-installer/).

First, enable nix flakes by adding the following to `~/.config/nix/nix.conf`
```
experimental-features = nix-command flakes
```

Then build the numerical analysis libraries:
```
cd sturdy.scala/sturdy-apron
nix build ..#numerical-analysis-libraries
ln -s result/lib lib
```

The result can be found in `sturdy.scala/sturdy-apron/result/lib`.

To run the tests add this path to the java library path:
```
java -Djava.library.path=$ProjectFileDir$/sturdy-apron/result/lib ...
```