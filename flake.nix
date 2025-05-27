{
  description = "sturdy.scala";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
    let pkgs = nixpkgs.legacyPackages.${system};
    in {
      packages = rec {
        apron = pkgs.callPackage sturdy-apron/apron.nix {};
        sturdy = pkgs.stdenv.mkDerivation {
          pname = "sturdy";
          version = "0.1";
          src = ./.;
          buildInputs = [ pkgs.sbt pkgs.jdk19_headless apron ];
          buildPhase = "sbt assembly";
          installPhase = ''
            mkdir -p $out/bin
            mkdir -p $out/share/java
            cp target/scala-*/*.jar $out/share/java
          '';
        };
      };
    });
}
