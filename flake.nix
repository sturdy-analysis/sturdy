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
          # Native apron/fenv libs must be re-linked in every sandboxed phase:
          # depsWarmupCommand and buildPhase each run against their own fresh
          # checkout of the source, so the symlink from one phase is never
          # visible in another.
          linkApronLib = ''
            rm -rf sturdy-apron/lib
            ln -s ${numerical-analysis-libraries}/lib sturdy-apron/lib
          '';
          sturdy = sbt.lib.mkSbtDerivation {
            pkgs = pkgs;
            pname = "sturdy";
            version = "0.1";
            src = ./.;
            depsWarmupCommand = ''
              ${linkApronLib}
              sbt update
            '';
            nativeBuildInputs = [ numerical-analysis-libraries ];
            depsSha256 = "sha256-QTg0xFFBjIZ8Bt5+HMMEq8nMYY8HYDgk9tmUDEX8lws=";

            buildPhase = ''
              ${linkApronLib}
              sbt compile
            '';

            installPhase = ''
              mkdir -p $out/sturdy
              cp -r ./* $out/sturdy/
            '';
          };
        in {
          devShells = {
            default = pkgs.mkShell {
              packages = [ jdk pkgs.sbt numerical-analysis-libraries ];
            };
          };
          packages = {
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
            inherit sturdy;
          } // pkgs.lib.optionalAttrs pkgs.stdenv.isLinux {
            # dockerTools images can only be built on Linux (busybox etc. aren't
            # packaged for Darwin); build one on a Linux machine/CI instead.
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
      devShells = forAllSystems (system: (perSystem system).devShells);
      packages = forAllSystems (system: (perSystem system).packages);
    };
}
