{ lib, stdenv, fetchFromGitHub, cmake }:

stdenv.mkDerivation rec {
  name = "binaryen";
  version = "112";

  src = fetchFromGitHub {
    owner = "WebAssembly";
    repo = "binaryen";
    rev = "version_${version}";
    sha256 = "sha256-vJ5Cf1r4hw1TQCJfS35/qG0wRL9vA9ZATwOZ96NmDY0=";
    fetchSubmodules = true;
  };

  nativeBuildInputs = [ cmake ];

  meta = with lib; {
    homepage = "https://github.com/WebAssembly/binaryen";
    description = "Optimizer and compiler/toolchain library for WebAssembly ";
    license = licenses.asl20;
    platforms = platforms.all;
  };
}