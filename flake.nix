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
            jre = prev.openjdk25; # make sure sbt picks up the JDK we want
          };
          pkgs = import nixpkgs {
            inherit system;
            overlays = [ overlay ];
          };
          jdk = pkgs.jre; # as defined in the overlay above
          numerical-analysis-libraries = pkgs.buildEnv {
            name = "numerical-analysis-libraries";
            paths = [
              pkgs.apron
#            elina doesn't build on mac
            ];
          };
          # Native apron libs must be re-linked in every sandboxed phase:
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
              sbt compile
            '';
            nativeBuildInputs = [ numerical-analysis-libraries ];
            depsSha256 = "sha256-QTg0xFFBjIZ8Bt5+HMMEq8nMYY8HYDgk9tmUDEX8lws=";

            buildPhase = ''
              ${linkApronLib}
              sbt \
                sturdy_core/assembly \
                sturdy_apron/assembly \
                sturdy_tip/assembly \
                sturdy_wasm/assembly \
                sturdy_jvm_bytecode/assembly
            '';

            installPhase = ''
              mkdir -p $out/sturdy
              cp ./sturdy-*/target/scala-*/sturdy-*.jar $out/sturdy/
            '';
          };
          # `depsSha256` is a fixed-output-derivation hash: once a given hash
          # has been built successfully, Nix trusts that store path forever
          # and never re-fetches/re-hashes it, even if the *declared* hash is
          # later changed to something else. That means a local `nix build`
          # can silently "succeed" with a stale/wrong depsSha256 as long as
          # you've ever built it before - hiding the exact hash-mismatch that
          # CI hits on its clean store.
          #
          # Building against a throwaway store (`--store local?root=...`)
          # would be the cleanest way to force a genuine re-fetch, but Nix
          # doesn't support *building* against a diverted/non-default local
          # store ("building using a diverted store is not supported on this
          # platform"). Instead, this script finds the sbt-dependencies FOD
          # derivation that `sturdy` depends on and deletes its cached output
          # (if present) so the next build is forced to re-run
          # `depsWarmupCommand` and re-hash the result, matching what a clean
          # CI store experiences.
          checkDepsHashScript = pkgs.writeShellScriptBin "check-deps-hash" ''
            set -euo pipefail
            drv="$(nix path-info --derivation "$PWD#sturdy")"
            deps_drv="$(nix-store -q --requisites "$drv" | grep -m1 -- '-sbt-dependencies\.tar\.zst\.drv$')"
            if [ -z "$deps_drv" ]; then
              echo "Could not find the sbt-dependencies FOD derivation" >&2
              exit 1
            fi
            deps_out="$(nix-store -q "$deps_drv")"
            if [ -e "$deps_out" ]; then
              # Anything still referencing the deps output (e.g. a leftover
              # extract-dependencies output from a prior build) keeps it
              # "alive" and blocks deletion, so delete referrers first.
              referrers="$(nix-store --query --referrers "$deps_out" || true)"
              echo "Deleting cached deps output to force re-fetch: $deps_out"
              nix-store --delete $referrers "$deps_out"
            fi
            echo "Rebuilding .#sturdy so depsWarmupCommand is genuinely re-run and re-hashed..."
            nix build "$PWD#sturdy" "$@"
          '';
        in {
          apps = {
            check-deps-hash = {
              type = "app";
              program = "${checkDepsHashScript}/bin/check-deps-hash";
            };
          };
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
      apps = forAllSystems (system: (perSystem system).apps);
      devShells = forAllSystems (system: (perSystem system).devShells);
      packages = forAllSystems (system: (perSystem system).packages);
    };
}
