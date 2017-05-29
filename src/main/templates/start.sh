#!/bin/sh
cd `dirname $0`
java -cp .:irongpm.jar de.hshannover.f4.trust.irongpm.IronGpm $*
