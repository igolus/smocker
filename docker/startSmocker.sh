#!/bin/sh

# Use --debug to activate debug mode with an optional argument to specify the port.
# Usage : standalone.sh --debug
#         standalone.sh --debug 9797

# By default debug mode is disabled.
export SMOCKER_DB_DIR=$1
sh /opt/jboss/wildfly/bin/standalone.sh `echo $@| cut -d " " -f2-`