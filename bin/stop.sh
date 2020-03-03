#!/bin/sh
ps -ef|grep LogServer | awk '{print $2}' |while read pid
do
  echo 'kill -9 '$pid
  kill -9 $pid
done
