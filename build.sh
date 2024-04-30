#!/bin/bash

JAVA_HOME=sdk/jdk1.6.0_45

# Stop building if an error occurs
set -e

echo "Setting up directories"
mkdir --parents assets classes src
rm -rf classes/*

echo "Compiling"
${JAVA_HOME}/bin/javac `find src -name '*'.java` -d classes \
    -source 1.3 -target 1.3 \
    -bootclasspath sdk/cldcapi11.jar:sdk/midpapi20.jar

echo "Creating jar"
${JAVA_HOME}/bin/jar cvfm in.jar manifest.mf -C classes . -C assets .

echo "Verifying"
${JAVA_HOME}/bin/java -jar sdk/proguard.jar @midlets.pro
rm in.jar 