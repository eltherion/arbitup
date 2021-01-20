#!/bin/bash

./sbt clean publishLocal

EXPECTED_AMMONITE_MD5="d621e481dbe7fbba6b5757338266e903"

ammonite_file=amm

function ammonite_file_md5 {
  openssl md5 < $ammonite_file|cut -f2 -d'='|awk '{print $1}'
}

if [ ! -f $ammonite_file ] || [ "$(ammonite_file_md5)" != "$EXPECTED_AMMONITE_MD5" ]; then
  echo 'downloading '$ammonite_file 1>&2
  curl -L --output "$ammonite_file" "https://github.com/lihaoyi/Ammonite/releases/download/2.3.8/2.13-2.3.8"
fi

test -f $ammonite_file || exit 1
if [ "$(ammonite_file_md5)" != "$EXPECTED_AMMONITE_MD5" ]; then
  echo 'bad Ammonite file!' 1>&2
  exit 1
fi

chmod +x ./amm

./amm Main.sc
