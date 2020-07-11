FROM jboss/wildfly

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent
ADD smocker-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/

COPY eclipselink-2.7.6.jar /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main
COPY module.xml /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main

USER root
RUN chown jboss:jboss /opt/jboss/wildfly/standalone/deployments/*
RUN chown jboss:jboss /opt/jboss/wildfly/bin/*
