#!/bin/sh

# $Header: /home/gbsmith/projects/ResCafe/ResCafe_devel/RCS/ResCafe.sh,v 1.2 2000/07/15 21:25:39 gbsmith Exp gbsmith $
#----------------------------------------------------------------------------
# $Log: ResCafe.sh,v $
# Revision 1.2  2000/07/15 21:25:39  gbsmith
# Convert args to abs paths then pass to ResCafe
#
# Revision 1.1  2000/07/15 20:48:25  gbsmith
# Initial revision
#
#
#----------------------------------------------------------------------------

RESCAFEHOME=/opt/java/classes/ResCafe

# Get data file abs paths
n=0
for arg
do
   if echo $arg | grep "^/" > /dev/null
   then
      # An abs path - keep it
      resfile[$n]=$arg
   else
      # An rel path - convert to abs
      resfile[$n]=$PWD/$arg
   fi

   n=$((n+1))
done


CLASSPATH=${RESCAFEHOME}/ResCafe.jar:${RESCAFEHOME}/plugins:${CLASSPATH}
#CLASSPATH=ResCafe.jar:plugins:${CLASSPATH}
cd $RESCAFEHOME


java ResCafe ${resfile[*]} &
