#!/bin/bash

OLD_JAVA_HOME=sdk/jdk1.6.0_45
JAVA_HOME=sdk/jdk-22.0.1

# Stop building if an error occurs
set -e

echo "Setting up directories"
mkdir --parents assets classes src
rm -rf classes/*

echo "Compiling"
PATHSEP=":"
[[ $(uname) == "Windows_NT" ]] && PATHSEP=";"

${OLD_JAVA_HOME}/bin/javac `find src -name '*'.java` -d classes \
    -source 1.3 -target 1.3 \
    -bootclasspath "sdk/cldcapi10.jar${PATHSEP}sdk/midpapi20.jar${PATHSEP}sdk/jsr75.jar"

echo "Creating jar"
${JAVA_HOME}/bin/jar cvfm in.jar manifest.mf -C classes . -C assets .

echo "Verifying"
${JAVA_HOME}/bin/java -jar sdk/proguard.jar @midlets.pro
rm in.jar 