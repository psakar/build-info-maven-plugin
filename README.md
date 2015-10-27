Maven plugin Build info
==


Prerequisities
===
java 8, maven 3.2+


Usage
===

1. Install maven plugin into your local
maven repository
run

   mvn install

2. Use plugin to create textfile with list of artifacts project builds

Go to folder of project where you want to know what artifacts are build and run plugin goal list-build-artifacts, this will create text file build_artifacts.txt in project build directory

cd MY_PROJECT
mvn clean package -DskipTests=true org.jboss:list-build-artifacts-maven-plugin:0.0.1:list-build-artifacts
cat target/build_artifacts.txt

3. To display build artifacts of multimodule project run

   find . -name "build_artifacts.txt" -exec cat {} \;
