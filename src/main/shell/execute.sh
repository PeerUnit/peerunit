#!/bin/bash
# 
# This file is part of PeerUnit.
# 
# PeerUnit is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# PeerUnit is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
#


source ${HOME}/.bashrc
set -m

echo "Cleaning old logs (if any)." 
rm *.log* 

# Globals
PROPERTIES="peerunit.properties"   # Properties file
SERVER=`hostname -f`

# Read properties file

# Number of testers per physical node
NTESTERS=`sed '/^\#/d'  peerunit.properties | grep 'tester.pernode' | tail -n 1 | sed 's/^.*=//'`

# Java classpath
CLASSPATH=`sed '/^\#/d'  peerunit.properties | grep 'tester.classpath' | tail -n 1 | sed 's/^.*=//'`

# Test Case (Java Class)
TEST=`sed '/^\#/d'  peerunit.properties | grep 'tester.testcase' | tail -n 1 | sed 's/^.*=//'`

# Write properties file

# Calculate total number of testers:
((PEERS= $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d | wc -l) * ${NTESTERS} ))

# Set number of testers
sed -i s/tester.peers=[[:alnum:]]*/tester.peers=${PEERS}/g ${PROPERTIES}

# Set server name
sed -i s/tester.server=[[:alnum:]].*/tester.server=${SERVER}/g ${PROPERTIES}

#	Execution.
#
echo "Server: $SERVER  peers: $PEERS nodes: " $(( $PEERS/$NTESTERS )) 
echo "Classpath: ${CLASSPATH}"

# Execute the Coordinator on the first node
java -classpath ${CLASSPATH} -ea fr.inria.peerunit.CoordinatorRunner peerunit.properties &


# for each node dismissing the first
for i in $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d) ;
do 
    echo oarsh $i "cd ${PWD} \; ./runTesters.sh ${TEST}  ${NTESTERS} ${CLASSPATH}" 
    oarsh $i "cd ${PWD} ; ./runTesters.sh ${TEST} ${NTESTERS} ${CLASSPATH}" & 
done

# Send first background process (coordinator) to foreground
fg %1

LOCAL=`echo $SERVER | awk -F. '{print $2}'`
DIR=logs/${TESTCASE}_${LOCAL}_${PEERS}peers${NTESTERS}th_${COORD}${BTREE}-`date +%Y%m%d%H%M` 
mkdir -p $DIR
mv *.log $DIR/ 

#fi
