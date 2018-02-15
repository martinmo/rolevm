#!/bin/sh
exec java --add-opens=java.base/java.io=ALL-UNNAMED -jar rolevm-bench/target/benchmarks.jar "$@"
