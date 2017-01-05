FROM jupyter/base-notebook

#ADD . /opt/groovy
#ADD backports.list /etc/apt/sources.list.d/backports.list
#WORKDIR /opt/groovy

ENV KERNEL_DIR /home/jovyan/.local/share/jupyter/kernels
ENV JAR jupyter-groovy-kernel-1.1.0.jar

USER root
#RUN chown -R jovyan:users /opt/groovy
ADD backports.list /etc/apt/sources.list.d/backports.list
RUN apt-get update && apt-get install -y openjdk-8-jdk
COPY ./startup.sh /usr/bin/starup
RUN chmod +x /usr/bin/starup

USER jovyan
#RUN ./mvnw package
#RUN mkdir -p /home/jovyan/.local/share/jupyter/kernels
#RUN ./install.sh /home/jovyan/.local/share/jupyter/kernels

RUN mkdir -p $KERNEL_DIR
COPY $JAR $KERNEL_DIR
COPY src/distribution/kernel.json /tmp/kernel.json
RUN cat /tmp/kernel.json | sed "s|__PATH__|$KERNEL_DIR/$JAR|" > $KERNEL_DIR/kernel.json
RUN jupyter kernelspec install --replace --user --name groovy $KERNEL_DIR

VOLUME ["/home/jovyan/work"]
WORKDIR /home/jovyan/work
