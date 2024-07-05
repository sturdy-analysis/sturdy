{ lib, stdenv, fetchFromGitHub, apron, gmp, mpfr, libccd, openjdk, perl }:

stdenv.mkDerivation rec {
  name = "elina";
  version = "1.3.2";

  src = fetchFromGitHub {
    owner = "eth-sri";
    repo = "ELINA";
    rev = "f524156d292ac3a6f3cd676e2d2e7db6629e2b6f";
    sha256 = "sha256-i33iCvsuUJq06gdqXQLKs/2/V8yc4hq+BvceECM9DGg=";
  };

  patchPhase = ''
    substituteInPlace ./configure \
      --replace 'if test $has_java -eq 1; then searchbin "javah"; has_java=$?; javah="$path"; fi' 'javah=""'

    sed -i '100s/make all/make install/' Makefile

    substituteInPlace ./java_interface/Makefile \
      --replace 'PREFIX = $(APRON_PREFIX)' 'PREFIX = $(ELINA_PREFIX)' \
      --replace 'export CLASSPATH=.:/usr/local/apron.jar:/usr/local/gmp.jar:$CLASSPATH' 'export CLASSPATH=${apron}/lib/apron.jar:${apron}/lib/gmp.jar' \
      --replace '-I$(APRON_SRCROOT)/apron' '-I${apron}/include' \
      --replace '-I$(APRON_SRCROOT)/japron/apron' "" \
      --replace '-I$(APRON_SRCROOT)/japron/gmp' "" \
      --replace '-I$(APRON_SRCROOT)/japron' "" \
      --replace '-I../elina_oct' "-I../elina_oct -I../elina_linearize -I../elina_auxiliary" \
      --replace '-L$(APRON_SRCROOT)/apron' '-L${apron}/lib' \
      --replace '-L$(APRON_SRCROOT)/japron/apron' "" \
      --replace '-L$(APRON_SRCROOT)/japron/gmp' "" \
      --replace '-L$(APRON_SRCROOT)/japron' "" \
      --replace '-L../elina_oct' "-L../elina_oct -L../elina_linearize -L../elina_auxiliary" \
      --replace '$(JAVAH) -o $@ elina.$*' '$(JAVAC) -h elina $+'



    substituteInPlace ./java_interface/elina/Test.java \
      --replace "env2.add(null,null);" ""
  '';

  configurePhase = ''
    ./configure \
       -prefix $out \
       -use-apron \
       -apron-prefix ${apron} \
       -apron-srcroot ${apron.src} \
       -gmp-prefix ${gmp} \
       -mpfr-prefix ${mpfr} \
       -cdd-prefix ${libccd} \
       -use-java \
       -java-prefix ${openjdk} \
  '';

  nativeBuildInputs = [ perl ];
  buildInputs = [ gmp mpfr libccd openjdk ];

  meta = with lib; {
    homepage = "https://elina.ethz.ch/";
    description = "ETH Library for Numerical Analysis ";
    license = licenses.lgpl3Only;
    platforms = platforms.all;
  };
}
