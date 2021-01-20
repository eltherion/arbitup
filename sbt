#!/bin/bash

EXPECTED_SBTJAR_MD5="5dbe0c5bbf9a2da701f7fae4b593aba9"

sbtjar=sbt-launch.jar

function sbtjar_md5 {
  openssl md5 < $sbtjar|cut -f2 -d'='|awk '{print $1}'
}

if [ ! -f $sbtjar ] || [ "$(sbtjar_md5)" != "$EXPECTED_SBTJAR_MD5" ]; then
  echo 'downloading '$sbtjar 1>&2
  curl -L --output "$sbtjar" "https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.4.6/$sbtjar"
fi

test -f $sbtjar || exit 1
if [ "$(sbtjar_md5)" != "$EXPECTED_SBTJAR_MD5" ]; then
  echo 'bad sbtjar!' 1>&2
  exit 1
fi

java -jar $sbtjar "$@"
