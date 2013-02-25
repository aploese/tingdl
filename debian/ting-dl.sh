#!/bin/sh

CLASSPATH="/usr/share/java/httpclient.jar:/usr/share/java/httpcore.jar:/usr/share/java/commons-logging.jar:/usr/share/java/commons-codec.jar:/usr/share/java/commons-cli.jar:/usr/share/java/slf4j-api.jar:/usr/share/java/slf4j-simple.jar:/usr/share/java/ting-dl.jar"

exec /usr/bin/java -classpath "$CLASSPATH" \
  -Dapp.name="ting-dl" \
  -Dapp.pid="$$" \
  net.sf.tingdl.Main \
  "$@"

