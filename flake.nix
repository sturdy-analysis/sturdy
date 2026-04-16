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
    flake-utils.lib.eachDefaultSystem (sys:
    let overlay = self: super: {
            apron = self.callPackage sturdy-apron/apron.nix {};
            elina = self.callPackage sturdy-apron/elina.nix {};
            fenv = self.callPackage sturdy-apron/fenv.nix {};
        };
        pkgs = import nixpkgs {
          system = sys;
          overlays = [ overlay ];
        };
        jdk = pkgs.jdk21_headless;
    in {
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
        numerical-analysis-libraries = pkgs.buildEnv {
          name = "numerical-analysis-libraries";
          paths = [
            apron
#            elina doesn't build on mac
            fenv
          ];
        };
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

          patchPhase = ''
            substituteInPlace soundness-tests.sh --replace-fail "java" "${jdk}/bin/java"
            substituteInPlace scalability-tests.sh --replace-fail "java" "${jdk}/bin/java"
            substituteInPlace precision-tests.sh --replace-fail "java" "${jdk}/bin/java"
          '';

          buildPhase = ''
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
    });
}
