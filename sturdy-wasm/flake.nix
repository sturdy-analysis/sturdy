{
    inputs = {
        nixpkgs.url = "github:nixos/nixpkgs";
    };

    outputs = { self, nixpkgs }:
    let pkgs = import nixpkgs { system = "x86_64-linux"; };
    in
    {
        packages.x86_64-linux.binaryen = pkgs.callPackage ./binaryen.nix {};
        packages.x86_64-linux.assemblyscript = pkgs.callPackage ./assemblyscript.nix {};
    };
}