#!/bin/sh

java -Djava.library.path=sturdy-apron/lib -Xss1G -Xms$RAM -Xmx$RAM -cp sturdy-wasm/target/scala*/sturdy-wasm.jar org.scalatest.tools.Runner -o -s sturdy.language.wasm.testscript.RelationalAnalysisSoundnessTests $@