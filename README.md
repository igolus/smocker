[![Build Status](https://travis-ci.com/igolus/smocker.svg?branch=master)](https://travis-ci.com/igolus/smocker)
[![Docker Repository](https://img.shields.io/badge/docker-igolus%2Fsmocker-blue?logo=docker)](https://hub.docker.com/r/igolus/smocker)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Visit http://www.smocker.org/

# What is Smocker ?

Smocker is an open source software allowing you to watch an mock all the network interactions made by your java application via a Rich Web application. You can easily discover how your java application deals with external network dependencies.

Then stub external networks dependencies to isolate you application and test all kind of behaviors of external systems.

![](https://github.com/igolus/smocker/blob/master/Docs/images/2019-03-13-21_30_46-Mozilla-Firefox-830x342.png?raw=true)

![](https://github.com/igolus/smocker/blob/master/Docs/images/2019-03-06-21_25_38-Mozilla-Firefox-1024x370.png?raw=true)

## Differences with other Service virtualization tools

### inside the JVM

All virtualization service tools are acting as proxy, recording and stubbing request and response are going through the proxy.

Smocker agent is different, it is directly embedded inside the JVM, no configuration change is needed to sniff and mock the network interactions.

### Deals with every connections

Classic virtualization tools needs dedicated port for each external system. Smocker watch every thing, in example both backend and database connections can be virtualized in the same place.

### Fully programmable

Scripting is available in both Java and Javascript languages. any kind of TCP protocol can then be implemented.

## High level description

![](https://raw.githubusercontent.com/igolus/smocker/master/Docs/images/smockerHighLevel2 (1).jpg)]()

Smocker is divided in two part :

### Smocker Client

- The client Use [javassist](http://www.javassist.org/) to decorate java socket layer allowing sniffing and stubbing network communications

### Smocker Server

- A web application running in a [Wildfly](http://wildfly.org/) server.
- GUI is built using [vaadin](https://vaadin.com/) framework and Rest services.
- J2V8 is used to allow programmatic interaction.

## Installation

### Prerequistes

-  download and install [WildFly](https://wildfly.org/) 
  - A customized version is available @ <http://www.smocker.org/>
- Java8
- Maven

### Compile the project

Fork the repo and clone it.

run mvn install in root folder.

find jar for the client in smockerAgent1.7\target

find war for wild fly in C:\java\git_clones\smocker\smockerVaadin\target

### Run smocker

#### Set Up

Let’s create environment variable to define the smocker client java argument line.

First define SMOCKER_CLIENT_HOME targeting the place where the client is installed

![](https://github.com/igolus/smocker/blob/master/Docs/images/env1-1.png?raw=true)

Then define the SMOCKER_JAVA_ARGS environment variable.

![](https://github.com/igolus/smocker/blob/master/Docs/images/env2.png?raw=true)

Here is the value to define

-Xbootclasspath/a:%SMOCKER_CLIENT_HOME%/javassist.jar;%SMOCKER_CLIENT_HOME%/smockerAgent1.7-1.0-SNAPSHOT.jar -javaagent:%SMOCKER_CLIENT_HOME%/smockerAgent1.7-1.0-SNAPSHOT.jar -Djava.util.logging.SimpleFormatter.format="%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n"

#### Run JVM with smocker

Then add java program you want to smock: java %SMOCKER_JAVA_ARGS% ....

#### Run Wildfly

##### With a local fat wildfly

Download the server part [here](https://www.smocker.org/install/)

Adapt the [Install Folder]/bin/standalone.bat or standalone.sh to set your Java Home (if needed)

Simply run standalone.bat or standalone.sh in [Smocker_Server]\wildfly-13.0.0.Final-Smocker\bin folder.

##### With Docker 

```
docker pull igolus/smocker
docker run -p 8080:8080 igolus/smocker
```

##### Enjoy

Use firefox to browse http://localhost:8080/smocker/

## Licence

Smocker use Apache License 2.0

 