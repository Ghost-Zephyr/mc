#!/bin/sh
[ $# -eq 0 ] && echo "No args dumbass!" && exit 1
#jar -c $1 > serv/plugins/$1.jar
wd=`pwd`
cd $1

mvn package
cp target/"$1"-*.*.?.jar ../serv/plugins

cd $wd

