{ lib, buildNpmPackage, fetchFromGitHub }:

buildNpmPackage rec {
  pname = "assemblyscript";
  version = "0.27.2";

  src = fetchFromGitHub {
    owner = "AssemblyScript";
    repo = pname;
    rev = "v${version}";
    hash = "sha256-WY+l1wWBlQllTJJ2E7A5EDfP9biZSs65ntAa1jKB0ik=";
  };

  npmDepsHash = "sha256-9ILa1qY2GpP2RckcZYcCMmgCwdXIImOm+D8nldeoQL8=";

  meta = with lib; {
    description = "A TypeScript-like language for WebAssembly";
    homepage = "www.assemblyscript.org";
    license = licenses.asl20;
  };
}