{ lib, stdenv, fetchFromGitHub, gmp, mpfr, ppl, openjdk }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "antoinemine";
    repo = "apron";
    rev = "3e9c221bb48b9edfbe20b95463498e8c475d32f6";
    sha256 = "sha256-+FDo4f+bXp8b8wQEtTYwdy1PWsYKNpKYJ8KVglZ1yjI=";
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
