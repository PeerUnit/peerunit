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

if [ $# -lt 2 ]
then
	echo Usage: runTesters.sh test  '[testers]' classpath
	echo Ex: runTesters.sh test.SimpleTest  10 ./path/to/my.jar
else
	for (( i=1; i<= $2; i++));
	do
		java -classpath $3 -ea  fr.inria.peerunit.TestRunner $1 &
	done
fi
