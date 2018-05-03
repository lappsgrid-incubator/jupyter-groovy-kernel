FROM jupyter/base-notebook

ARG VERSION
ENV KERNEL_DIR /opt/groovy
ENV JAR jupyter-groovy-kernel-${VERSION}.jar

USER root
ADD backports.list /etc/apt/sources.list.d/backports.list
RUN apt-get update && apt-get install -y openjdk-8-jdk

RUN mkdir -p $KERNEL_DIR
COPY $JAR $KERNEL_DIR/$JAR
COPY kernel.json $KERNEL_DIR/kernel.json
RUN jupyter kernelspec install --name groovy $KERNEL_DIR

# Fix: Permission denied: '/home/jovyan/.local/share'
RUN chown -R $NB_UID /home/jovyan/.local

USER $NB_UID
