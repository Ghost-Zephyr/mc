#!/bin/sh
[ $# -eq 0 ] && echo "No args dumbass!" && exit 1
wd=`pwd`
cd $1
mvn clean
cd $wd

