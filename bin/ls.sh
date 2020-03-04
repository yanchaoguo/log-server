#!/bin/bash

project=log-server
port=9001
kafka=hadoop006:9092,hadoop007:9092,hadoop008:9092

export BASE_DIR=$(dirname $0)/

export JAR_DIR=/data/work/log_server
export LOG_DIR=$JAR_DIR/logs
export PID_DIR=$JAR_DIR
export JAVA_OPS="-Xms2048m -Xmx2048m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -server -XX:+UseG1GC -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50"
#export JAVA_OPS="-Xms1024m -Xmx1024m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
export CLASSPATH=.././classes:.././lib/*

if [ "$1" = "start" ] ; then
eval java ${JAVA_OPS} -Dserver.type=n -Dserver.port=$port -Dkafka.servers=$kafka com.eqxiu.logserver.LogServer > $LOG_DIR/$project-$port.log  2>&1 "&"
echo $! > "$PID_DIR/$project-$port.pid"
elif [ "$1" = "stop" ] ; then
kill `cat "${PID_DIR}/$project-$port.pid"`
rm -f "${PID_DIR}/$project-$port.pid"
fi