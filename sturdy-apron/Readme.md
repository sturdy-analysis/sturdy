You must build and install apron.

```
git clone https://github.com/antoinemine/apron
cd apron
./configure -gmp-prefix /opt/homebrew -mpfr-prefix /opt/homebrew -ppl-prefix /opt/homebrew -glpk-prefix /opt/homebrew -no-cxx --ext-dll dylib
make
make install
```
