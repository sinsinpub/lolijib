#!/bin/sh

prefix=$1
baseurl=http://jibjib.googlecode.com/svn/d/

for i in dist/$prefix-*.jad; do
  device_id=`basename $i .jad | sed s/jibjib-beta-//`;
  device_name=`echo $device_id | tr _ " "`;
  echo "* $device_name [jad]($baseurl$prefix-$device_id.jad) [jar]($baseurl$prefix-$device_id.jar)"
done
