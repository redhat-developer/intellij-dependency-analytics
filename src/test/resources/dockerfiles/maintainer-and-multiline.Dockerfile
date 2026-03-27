FROM dummy/jdk-oracle
MAINTAINER Alen Bob <alen.bobn@live.com>

ENV JAVA_HEAP_SIZE 512
ENV JAVA_HOME /usr/lib/jvm/java-7-oracle

RUN \
  apt-get update && \
  apt-get -y install \
          tomcat7 && \
  rm -rf /var/lib/apt/lists/*

RUN sed -i "s|#JAVA_HOME=.*|JAVA_HOME=$JAVA_HOME|g" /etc/default/tomcat7
RUN sed -i "s|-Xmx128m|-Xmx${JAVA_HEAP_SIZE}m|g" /etc/default/tomcat7
