# Docker Container

## TL;DR

```bash
./build.sh
```
 -or-
 
```bash
docker build --build-arg VERSION=x.y.z -t lappsgrid/igroovy .
```
 
Where *x.y.z* is the version of the jar file that will be included in the Docker image.
 
**NOTE** The Dockerfile uses the variable *$VERSION* which must be defined with the `--build-arg` option to `docker`, For example, if the jar is named `jupypter-groovy-kernel-1.1.1.jar` run
 
```bash
docker build --build-arg VERSION=1.1.1 -t lappsgrid/igroovy .
```
 

## Building An Image
 
Since Docker copies the entire contents of the current directory to the Docker daemon when performing a build we keep the files requied by Docker separate from the rest of the project.

The *build.sh* script simply copies the most recent jar and kernel.json files to the current directory and sets the *VERSION* variable before running the Docker build.

