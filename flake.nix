{
  description = "sturdy.scala";

  inputs = {
    self.submodules = true;
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";
    flake-utils.url = "github:numtide/flake-utils";
    sbt = {
      url = "github:zaninime/sbt-derivation";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils, sbt }:
    let
      forAllSystems = nixpkgs.lib.genAttrs flake-utils.lib.defaultSystems;
      perSystem = system:
        let
          overlay = final: prev: {
            apron = final.callPackage sturdy-apron/apron.nix {};
            elina = final.callPackage sturdy-apron/elina.nix {};
            fenv = final.callPackage sturdy-apron/fenv.nix {};
          };
          pkgs = import nixpkgs {
            inherit system;
            overlays = [ overlay ];
          };
          jdk = pkgs.jdk21_headless;
          numerical-analysis-libraries = pkgs.buildEnv {
            name = "numerical-analysis-libraries";
            paths = [
              pkgs.apron
#            elina doesn't build on mac
              pkgs.fenv
            ];
          };
          ciCompileScript = pkgs.writeShellScriptBin "ci-compile" ''
            set -euo pipefail
            rm -rf sturdy-apron/lib
            ln -s ${numerical-analysis-libraries}/lib sturdy-apron/lib
            sbt compile
          '';
        in {
          apps = rec {
            ci-compile = {
              type = "app";
              program = "${ciCompileScript}/bin/ci-compile";
            };
            default = ci-compile;
          };
          devShells = {
            default = pkgs.mkShell {
              packages = [ jdk pkgs.sbt numerical-analysis-libraries ciCompileScript ];
            };
            ci = pkgs.mkShell {
              packages = [ jdk pkgs.sbt numerical-analysis-libraries ciCompileScript ];
            };
          };
          packages = rec {
            pyenv = pkgs.python3.withPackages (ps: with ps; [
              jupyter
              ipython
              pandas
              seaborn
            ]);
            apron = pkgs.apron;
            elina = pkgs.elina;
            fenv = pkgs.fenv;
            inherit numerical-analysis-libraries;
            sturdy = sbt.lib.mkSbtDerivation {
              pkgs = pkgs;
              pname = "sturdy";
              version = "0.1";
              src = ./.;
              depsWarmupCommand = ''
                rm -rf sturdy-apron/lib
                ln -s ${numerical-analysis-libraries}/lib sturdy-apron/lib
                sbt compile
              '';
              nativeBuildInputs = [ numerical-analysis-libraries ];
              depsSha256 = "sha256-ZllRXxIE6qomVoRj0t8pBPLH9sslLUmU9Dxc6pv0eew=";

              buildPhase = ''
                rm -rf sturdy-apron/lib
                ln -s ${numerical-analysis-libraries}/lib sturdy-apron/lib
                sbt sturdy_wasm/Test/assembly
              '';

              installPhase = ''
                mkdir -p $out/sturdy
                cp -r ./* $out/sturdy/
              '';
            };
            docker = pkgs.dockerTools.buildLayeredImage {
              name = "sturdy";
              tag = "latest";
              contents = [
                sturdy jdk pkgs.bash pkgs.coreutils pkgs.busybox
              ];
              config = {
                Env = [ "PATH=/bin:${pkgs.bash}/bin:${pkgs.coreutils}/bin:${pkgs.busybox}/bin" ];
                Cmd = "${pkgs.bash}/bin/bash";
                WorkingDir = "/sturdy/";
              };
            };
          };
        };
    in {
      apps = forAllSystems (system: (perSystem system).apps);
      devShells = forAllSystems (system: (perSystem system).devShells);
      packages = forAllSystems (system: (perSystem system).packages);
    };
}
