{ lib, stdenv, fetchFromGitHub, gmp, mpfr, ppl, openjdk }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "antoinemine";
    repo = "apron";
    rev = "4bb90a48fac10e64a72bfad2875ecae848584cc0";
    sha256 = "sha256-9WBKWxGvuUJNYu21QfHYZuJL+FUXGKm38lFdFsvkS2E=";
  };

  configurePhase = ''
    ./configure -prefix $out
  '';

  buildInputs = [ gmp mpfr ppl openjdk ];

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
