{ lib, stdenv, binutils, fetchFromGitHub, gmp, mpfr, ppl, jdk21_headless }:

stdenv.mkDerivation rec {
  name = "apron";
  version = "0.9.13";

  src = fetchFromGitHub {
    owner = "svenkeidel";
    repo = "apron";
#    rev = "4bb90a48fac10e64a72bfad2875ecae848584cc0";
#    sha256 = "sha256-9WBKWxGvuUJNYu21QfHYZuJL+FUXGKm38lFdFsvkS2E=";
#    rev = "ee3a634637b0caf261b83067b231636d3165babd";
#    sha256 = "sha256-7pN1VGcPuofNFd5Yse8j8tyRBWEfpmnfV+mNIFEB2kw=";
    rev = "165dc0ebfa96f378b772e11eca556c47fdc64285";
    sha256 = "sha256-WyKPh60OegHn/jfL9sdms27nSVm/XVvqlQyajbJH7Ec=";
  };

  patchPhase = ''
    substituteInPlace configure \
      --replace "strip=\"strip --strip-unneeded\"" "strip=\"${binutils}/bin/strip --strip-unneeded\""
  '';

  configurePhase = ''
    ./configure -prefix $out
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
