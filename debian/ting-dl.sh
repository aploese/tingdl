#!/bin/sh

if [ "$1" = "-classpath" ]
then
  CLASSPATH="$2"
  shift 2
fi

CLASSPATH="${CLASSPATH:-.}:/usr/share/java/httpclient.jar:/usr/share/java/httpcore.jar:/usr/share/java/commons-logging.jar:/usr/share/java/commons-codec.jar:/usr/share/java/ting-dl.jar"
export CLASSPATH

exec /usr/bin/java net.sf.tingdl.Main "$@"
