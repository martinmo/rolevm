#!/bin/sh
exec java --add-opens=java.base/java.io=ALL-UNNAMED -jar rolevm-bench/target/benchmarks.jar -f 1 -wi 10 -i 10 "$@"
