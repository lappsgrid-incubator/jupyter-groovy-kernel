# Rolling Out A Release

This page is intended for *Release Managers*, that is, people that have the proper credentials to:

1. Commit changes to the *master* branch on GitHub.
1. Have a public certificate installed on lappsgrid.org so they can upload tarballs.
1. Have credentials to push Docker images to the Docker Hub, and finally
1. Have a user account and permissions to push org.lappsgrid artifacts to Maven Central

## TL;DR

Assume we are releasing version 1.2.0

```bash
git flow release start 1.2.0
mvn versions:set -DnewVersion=1.2.0 -DgeneratebackupPoms=false
cd src/docker
./build.sh
docker tag lappsgrid/jupyter-groovy-kernel lappsgrid/jupyter-groovy-kernel:1.20
docker push lappsgrid/jupyter-groovy-kernel
docker push lappsgrid/jupyter-groovy-kernel:1.20
cd ../../src/distribution
./create.sh
cd ../
./release.sh --site
git flow release finish 1.20
git push --tags
git push origin master
```

It is up to the person performing the release to ensure the project does not include any SNAPSHOT dependencies.

## The Long Story

There are a number of steps required to complete a new release:

1. Create and deploy the Docker images.
1. Create and upload a tarball of the kernel to lappsgrid.org
1. Generate the Maven site and deploy it to the gh-pages branch of the repository.
1. Deploy the Maven artifacts to Maven Central.
1. Upate the master branch on GitHub and create a Git tag of the current state of master.


### Git Flow

While not strictly required, these instructions assume you are using [Git Flow](https://github.com/nvie/gitflow) to handle the Git branching, merging and tagging.  If you don't or can't use Git Flow simply create a "release branch" for performing the release tasks and then merge the release branch into *master* when finished. Don't forget to *tag* the version on GitHub as part of the release if not using Git Flow.

```bash
git flow release start 1.2.0
...work...work...work...
git commit -a -m "Release ready."
git flow release finish 1.2.0
```
-or-
```bash
git checkout -b 1.2.0
...work...work...work...]
git commit -a -m "Release ready."
git checkout master
git merge 1.2.0
git push origin master
git branch -d 1.2.0
git tag -a -m "Release v1.2.0"
git push --tags
git checkout develop
git merge master
git push origin develop
```

After the release branch has been created either use the Maven Versions plugin to change the version number in the pom.xml file or edit the file manually. 

```bash
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false
```
 
### Docker
 
The `build.sh` script used to generate the Docker images does four things:

1. Gets a copy of the most current JAR file.
1. Creates a kernel.json file that references the current JAR.
1. Sets the VERSION arg for the Dockerfile.
1. Runs `docker build`

Once the image has been generated it needs to be tagged and pushed to the Docker Hub.

### Downloadable Tarball

To be written.  See the `src/distribution/create.sh` script for details.


### Maven Central

The Lappsgrid parent pom is set up so PGP signatures are generated for all artifacts if the Maven variable gpg.passphrase has been defined.  The deploy plugin has also been configured to deploy SNAPSHOT versions to the Sonatype Snapshot Repository and release builds to Maven Central.

```bash
mvn -Dgpg.passphrase=top_secret package source:jar deploy
```

The GroovyDoc Maven Plugin is run during the *package* phase and does not need to be run separately.
 
### Deploying The Maven Site

The [GitHub Site Plugin](https://github.github.com/maven-plugins/site-plugin/) is used to deploy the Maven generated site to the gh-pages branch on GitHub.
 
 ```bash
 mvn site
 ```




