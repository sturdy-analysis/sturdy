
setze `apron_prefix=/usr` in apron ./configure // standardmäßig auf /usr/local, unter WSL2 Ubuntu NICHT in `java.library.path`
`JAVA_HOME=$JAVA_HOME ./configure -no-ppl` // ppl ist nicht trivial installierbar
 `make -I$JAVA_HOME/include -I$JAVA_HOME/include/linux` // für jni


 ```
 apt install mlocate openjdk-11-jdk
 ```
 apron:
`sudo make install`


// -Djava.library.path=/usr/local/lib   // für apron/japron, falls pfad nicht geändert

