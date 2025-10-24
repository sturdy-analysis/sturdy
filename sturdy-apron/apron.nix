{ lib, stdenv, binutils, fetchFromGitHub, clang_14, gmp, mpfr, ppl, jdk21_headless, zip }:

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
      --replace "strip=\"strip --strip-unneeded\"" "strip=\"${binutils}/bin/strip --strip-unneeded\""
    substituteInPlace japron/Makefile \
      --replace "\$(CC) \$(CFLAGSN) -c \$(IFLAGS) \$< -o \$@" "\$(CC) \$(CFLAGSN) -c \$(IFLAGS) \$< -o \$@ && \$(CC) \$(CFLAGSN) -S -emit-llvm \$(IFLAGS) \$< -o \$(patsubst %.o,%.s,\$@)" \
      --replace "\$(CC) \$(CFLAGSN) -c \$(IFLAGS) -Igmp \$< -o \$@" "\$(CC) \$(CFLAGSN) -c \$(IFLAGS) -Igmp \$< -o \$@ && \$(CC) \$(CFLAGSN) -S -emit-llvm \$(IFLAGS) -Igmp \$< -o \$(patsubst %.o,%.s,\$@)" \
      --replace "\$(INSTALLd) \$(APRON_LIB)" "\$(INSTALLd) \$(APRON_LIB) && (find gmp -name '*.s' | ${zip}/bin/zip gmp.zip -@) && \$(INSTALL) gmp.zip \$(APRON_LIB) && (find apron -name '*.s' | ${zip}/bin/zip apron.zip -@) && \$(INSTALL) apron.zip \$(APRON_LIB)"
    cat -e -t -v japron/Makefile
  '';

  configurePhase = ''
    CC=${clang_14}/bin/clang ./configure -prefix $out
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
