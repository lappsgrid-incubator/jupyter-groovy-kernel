FROM jupyter/base-notebook

ADD . /opt/groovy
ADD backports.list /etc/apt/sources.list.d/backports.list
WORKDIR /opt/groovy

USER root
RUN chown -R jovyan:users /opt/groovy
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk

USER jovyan
RUN ./mvnw package
RUN mkdir -p /home/jovyan/.local/share/jupyter/kernels
RUN ./install.sh /home/jovyan/.local/share/jupyter/kernels

VOLUME ["/home/jovyan/work"]
WORKDIR /home/jovyan/work
