{
  description = "sturdy.scala";

  inputs.nixpkgs.url = github:NixOS/nixpkgs/nixos-unstable;
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (sys:
    let overlay = self: super: {
            apron = self.callPackage sturdy-apron/apron.nix {};
            elina = self.callPackage sturdy-apron/elina.nix {};
        };
        pkgs = import nixpkgs {
          system = sys;
          overlays = [ overlay ];
        };
    in {
      packages = rec {
        apron = pkgs.apron;
        elina = pkgs.elina;
        numerical-analysis-libraries = pkgs.buildEnv {
          name = "numerical-analysis-libraries";
          paths = [
            apron
            elina
          ];
        };
        sturdy = pkgs.stdenv.mkDerivation {
          pname = "sturdy";
          version = "0.1";
          src = ./.;
          buildInputs = [ pkgs.sbt pkgs.jdk19_headless apron elina ];
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
