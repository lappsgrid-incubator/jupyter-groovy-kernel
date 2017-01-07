# Building A Docker Image

## TL;DR

```bash
./build.sh
```
-or-
 
```bash
docker build --build-arg VERSION=x.y.z -t lappsgrid/jupyter-groovy-kernel .
```
 
Where *x.y.z* is the version of the jar file that will be included in the Docker image. For example, if the jar is named `jupypter-groovy-kernel-1.1.1.jar` run
 
```bash
docker build --build-arg VERSION=1.1.1 -t lappsgrid/jupyter-groovy-kernel .
```
 

## The Long Version
 
Since Docker copies the entire contents of the current directory to the Docker daemon when performing a build the files requied by Docker are kept in a separate src directory. The *build.sh* script copies the most recent jar and kernel.json files to the current directory and sets the *VERSION* variable before running the Docker build.

## Running The Image

```bash
docker run -p 8888:8888 lappsgrid/jupyter-groovy-kernel
```

If you would like to save your notebooks outside of the Docker container you will need to mount a directory as `/home/jovyan/work`.

```bash
LOCAL_DIR=/path/to/your/notebooks
docker run -p 8888:8888 -v $LOCAL_DIR:/home/jovyan/work lappsgrid/jupyter-groovy-kernel
```
<hr/>



