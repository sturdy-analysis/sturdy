{ lib, stdenv, gcc, openjdk }:

stdenv.mkDerivation rec {
  name = "fenv";
  version = "0.0.1";

  src = ./src/main/jni;

  configurePhase = "";

  nativeBuildInputs = [ gcc openjdk ];

  buildPhase = ''
    gcc -lm -shared -o ./libfenv.so -fPIC fenv_FEnv.c
  '';

  installPhase = ''
    install -d $out/include/
    install fenv_FEnv.h $out/include/

    install -d $out/lib/
    install libfenv.so $out/lib/
  '';
}
