#!/bin/sh
[ $# -eq 0 ] && echo "No args dumbass!" && exit 1
#jar -c $1 > serv/plugins/$1.jar

until [ "$sel" = "0" ]; do
  echo "What kind of plugin are we building?"
  echo "    	1  -  Paper plugin"
  echo "    	2  -  Waterfall plugin"
  echo "    	0  -  Cancel build"
  echo -n "  Enter selection: "
  read sel
  case $sel in
    1 ) break ;;
    2 ) break ;;
    0 ) exit ;;
    * ) echo "Invalid input!" ;;
  esac
done
wd=`pwd`
cd $1
mvn package
if [ "$?" != "0" ]; then
  echo "Build failed!"
	echo "Exiting."
  exit 1
fi
if [ "$sel" = "2" ]; then
	cp target/shaded-*.jar ../waterfall/plugins
else
	cp target/*-shaded.jar ../serv/testing/plugins
fi

cd $wd

