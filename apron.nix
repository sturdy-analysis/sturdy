{ lib, stdenv, fetchFromGitHub, gmp, mpfr, ppl, openjdk }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "antoinemine";
    repo = "apron";
    rev = "1a8e91062c0d7d1e80333d19d5a432332bbbaec8";
    sha256 = "sha256-hAbGgMe7zxApgHa7Qd3gKpBdFE4Ak1BtpmzZjEZAXDc=";
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