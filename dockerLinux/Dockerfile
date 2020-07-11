FROM jboss/wildfly

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

ADD smocker-1.0-SNAPSHOT_linux.war /opt/jboss/wildfly/standalone/deployments/

COPY eclipselink-2.7.6.jar /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main

COPY module.xml /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main
COPY standalone.conf /opt/jboss/wildfly/bin
COPY libj2v8_linux_x86_64.so /opt/jboss/wildfly/bin

USER root
RUN chown jboss:jboss /opt/jboss/wildfly/standalone/deployments/*
RUN chown jboss:jboss /opt/jboss/wildfly/bin/*
