FROM ubuntu:16.04
MAINTAINER  igolus

# update and install dependencies
RUN         	apt-get update \
				&& apt-get install -y \
                    software-properties-common \
                    wget \
				&& add-apt-repository -y ppa:ubuntu-toolchain-r/test \
                && apt-get update \
				&& apt-get install -y \
                    curl \
				&& apt-get install -y \
                    gcc-4.9 g++-4.9 gcc-4.9-base \
				&& update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 100 \
                && update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.9 100

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 13.0.0.Final
ENV JBOSS_HOME /opt/jboss/wildfly

# Set the JAVA_HOME variable to make it clear where Java is located
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# Execute system update
# openjdk and wildfly will be installed
RUN apt-get update \
	&& apt-get -y install xmlstarlet bsdtar unzip curl wget openjdk-8-jdk \
	&& cd $HOME \
	&& curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
	&& tar xf wildfly-$WILDFLY_VERSION.tar.gz \
	&& mkdir -p $JBOSS_HOME \
	&& mv $HOME/wildfly-$WILDFLY_VERSION/* $JBOSS_HOME/ \
	&& rm wildfly-$WILDFLY_VERSION.tar.gz \
	&& mkdir -p $JBOSS_HOME/modules/com/oracle/ojdbc7/main/ \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Expose the ports we're interested in
EXPOSE 80
RUN mkdir ~/smockerDb
ENV SMOCKER_DB_DIR="~/smockerDb"
RUN echo "USING DB DIR $smockerdbdir"
ENV JAVA_OPTS="-server -Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -Djava.library.path=."
COPY standalone.sh /opt/jboss/wildfly/bin/

COPY startSmocker.sh /opt/jboss/wildfly/bin/


# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interfaces
ENTRYPOINT /opt/jboss/wildfly/bin/startSmocker.sh ${SMOCKER_DB_DIR:-.} -b 0.0.0.0

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 --silent
ADD smocker-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/

COPY eclipselink-2.7.6.jar /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main
COPY module.xml /opt/jboss/wildfly/modules/system/layers/base/org/eclipse/persistence/main
COPY libj2v8_linux_x86_64.so /opt/jboss/wildfly/bin