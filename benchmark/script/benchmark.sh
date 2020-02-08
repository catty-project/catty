#!/bin/sh

# benchmark script.
# usage: sh benchmark.sh

set -e
set -x

SCRIPT_DIR=$(cd "$(dirname "$0")";pwd)
JAR_PATH=$SCRIPT_DIR/../target/catty-benchmark-0.1.0-jar-with-dependencies.jar

JAVA_BIN=$(which java)
nohup $JAVA_BIN -jar $JAR_PATH 2>&1 &

sleep 2

JAVA_PID=$!

if [[ $? != 0 ]]; then
    echo "Error occurred!"
    exit 1;
fi

wrk -t4 -c500 -d60s -T3 --script=./wrk.lua --latency http://localhost:8088/i

if [[ $? != 0 ]]; then
    kill -9 $JAVA_PID
    echo "Wrk error occurred!"
    exit 1;
fi

kill -9 $JAVA_PID
