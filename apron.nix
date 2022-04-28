{ lib, stdenv, fetchFromGitHub, gmp, mpfr, ppl, openjdk }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "antoinemine";
    repo = "apron";
    rev = "refs/tags/v${version}";
    sha256 = "ftPjpQHWCpsPbe/F8McKyHb6W2lpRo5UM0L2hqGS1ZM=";
  };

  configurePhase = ''
    ./configure -prefix $out
  '';

  buildInputs = [ gmp mpfr ppl openjdk ];

  meta = with lib; {
    homepage = "https://antoinemine.github.io/Apron/doc/";
    description = "Numerical Abstract Domain Library";
    license = licenses.lgpl21Only;
    platforms = platforms.all;
  };
}