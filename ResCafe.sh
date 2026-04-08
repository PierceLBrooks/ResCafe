#!/bin/sh

APPDIR=`dirname $0`;
cd $APPDIR

JIMIPATH=/opt/java/classes/JimiProClasses.zip
CLASSPATH=ResCafe.jar:plugins:${JIMIPATH}:${CLASSPATH}

java ResCafe $* &
