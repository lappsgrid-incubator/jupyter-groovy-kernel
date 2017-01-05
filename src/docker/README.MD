# Docker Container

## TL;DR

```bash
./build.sh
```
 -or-
 
 ```bash
 docker build -t lappsgrid/igroovy .
 ```
 
 ## Building a Container
 
Since Docker copies the entire contents of the current directory to the Docker daemon when performing a build we keep the files requied by Docker separate from the rest of the project.

The *build.sh* script simply copies the most recent jar and kernel.json files to the current directory before running the Docker build.

