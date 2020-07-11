FROM jboss/wildfly
#ENV dbFolder .

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

ADD smocker-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/

COPY eclipselink-2.7.6.jar /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main

COPY module.xml /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main

COPY standalone.conf /opt/jboss/wildfly/bin
#RUN sed -i "s|-Dsmocker.dbdir=.|-Dsmocker.dbdir=${dbFolder} |g" /opt/jboss/wildfly/bin/standalone.conf


COPY libj2v8_linux_x86_64.so /opt/jboss/wildfly/bin

USER root
RUN chown jboss:jboss /opt/jboss/wildfly/standalone/deployments/*
RUN chown jboss:jboss /opt/jboss/wildfly/bin/*
