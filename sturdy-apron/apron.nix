{ lib, keepBuildTree, stdenv, binutils, fetchFromGitHub, clang_14, gmp, mpfr, ppl, jdk21_headless, zip }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "antoinemine";
    repo = "apron";
    rev = "2d0027326748b81026e2e8a711913be748b6ecca";
    sha256 = "sha256-32dm22P2RAFtRTOCmETaBS9rstv80lq3hvl4He9IgsY=";
  };

  patchPhase = ''
    substituteInPlace configure \
      --replace "strip=\"strip --strip-unneeded\"" "strip=\"${binutils}/bin/strip --strip-unneeded\"" \
      --replace 'acc=""' 'acc="-Wl,-plugin-opt=save-temps"'
  '';

  configurePhase = ''
    CC=${clang_14}/bin/clang CFLAGS=-flto LDFLAGS="-flto -fuse-ld=gold -Wl,-plugin-opt=save-temps" ./configure -prefix $out -no-cxx -ppl-prefix ${ppl}/lib/
  '';

  buildInputs = [ gmp mpfr ppl jdk21_headless ];

  postInstall = ''
    install japron/apron/japron.h japron/gmp/jgmp.h $out/include/
  '';

  meta = with lib; {
    homepage = "https://antoinemine.github.io/Apron/doc/";
    description = "Numerical Abstract Domain Library";
    license = licenses.lgpl21Only;
    platforms = platforms.all;
  };
}
