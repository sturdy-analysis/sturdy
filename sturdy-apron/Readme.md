You must build and install apron using the nix package manager (https://nixos.org/download/).

```
cd sturdy.scala/sturdy-apron
nix build ..#numerical-analysis-libraries
```

The result can be found in `sturdy.scala/sturdy-apron/result/lib`.

To run the tests add this path to the java library path:
```
java -Djava.library.path=$ProjectFileDir$/sturdy-apron/result/lib ...
```