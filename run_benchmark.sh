#!/bin/sh
exec java -Duser.language=en -Duser.country=US --add-opens=java.base/java.io=ALL-UNNAMED \
    -jar rolevm-bench/target/benchmarks.jar "$@"
