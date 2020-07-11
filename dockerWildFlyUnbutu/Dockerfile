FROM        ubuntu:xenial
MAINTAINER  igolus

# update and install dependencies
RUN         apt-get update \
                && apt-get install -y \
                    software-properties-common \
                    wget \
                && add-apt-repository -y ppa:ubuntu-toolchain-r/test \
                && apt-get update \
                && apt-get install -y \
                    make \
                    git \
                    curl \
                    vim \
                    vim-gnome \
                && apt-get install -y cmake=3.5.1-1ubuntu3 \
                && apt-get install -y \
                    gcc-4.9 g++-4.9 gcc-4.9-base \
                    gcc-4.8 g++-4.8 gcc-4.8-base \
                    gcc-4.7 g++-4.7 gcc-4.7-base \
                    gcc-4.6 g++-4.6 gcc-4.6-base \
                && update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.9 100 \
                && update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.9 100
				
				
ENV WILDFLY_VERSION 13.0.0.Final
ENV WILDFLY_SHA1 c0dd7552c5207b0d116a9c25eb94d10b4f375549
ENV JBOSS_HOME /opt/jboss/wildfly

# Set the JAVA_HOME variable to make it clear where Java is located
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

ADD assets /tmp

# Execute system update
# openjdk and wildfly will be installed
RUN apt-get update \
	&& apt-get -y install xmlstarlet bsdtar unzip curl wget openjdk-8-jdk \
	&& cd $HOME \
	&& curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
	&& sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
	&& tar xf wildfly-$WILDFLY_VERSION.tar.gz \
	&& mkdir -p $JBOSS_HOME \
	&& mv $HOME/wildfly-$WILDFLY_VERSION/* $JBOSS_HOME/ \
	&& rm wildfly-$WILDFLY_VERSION.tar.gz \
	&& mkdir -p $JBOSS_HOME/modules/com/oracle/ojdbc7/main/ \
	&& mv /tmp/ojdbc7-12.1.0.1.jar $JBOSS_HOME/modules/com/oracle/ojdbc7/main/ojdbc7.jar \
	&& mv /tmp/module.xml $JBOSS_HOME/modules/com/oracle/ojdbc7/main/ \
	&& mv /tmp/waitfordeployment.sh /opt/jboss/wildfly/bin/ \
	&& chmod 777 /opt/jboss/wildfly/bin/waitfordeployment.sh \
	&& rm -rf /tmp/* \
	&& apt-get clean \
	&& rm -rf /var/lib/apt/lists/*

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Expose the ports we're interested in
EXPOSE 80

# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]