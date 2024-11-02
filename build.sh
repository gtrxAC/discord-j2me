#!/bin/bash

# Path to Java JDK. The JDK listed here must support targeting Java 1.2 (J2ME).
# For example OpenJDK 8 (https://adoptium.net/temurin/releases/?version=8) or JDK 1.6.0_45
JAVA_HOME=sdk/jdk8u422-b05

# Project name, used as output JAR file name. By default this is the name of the current directory.
PROJECT_NAME=$(realpath $(pwd) --relative-to=..)

# Stop building if an error occurs
set -e

# Create directories if they don't already exist, and remove any leftover files.
echo "Setting up directories"
mkdir --parents bin classes res src
rm -rf classes/*

# Generate language files
if command -v node > /dev/null; then
    echo "Generating translations"
    cd language
    [[ -e node_modules ]] || npm i
    node convert.js
    cd ..
fi

# Compile for Java 1.2. Most (but not all) J2ME devices support Java 1.3
# If you want to change the APIs (bootclasspath), also edit the ProGuard config (midlets.pro)
echo "Compiling"
${JAVA_HOME}/bin/javac `find src -name '*'.java` -d classes \
    -source 1.2 -target 1.2 -Xlint:-options \
    -bootclasspath sdk/lib/javapiglerapi.jar:sdk/lib/jsr75.jar:sdk/lib/midpapi20.jar:sdk/lib/cldcapi10.jar \
    > sdk/log.txt

echo "Creating JAR"
${JAVA_HOME}/bin/jar cvfm bin/in.jar manifest.mf -C classes . -C res . >> sdk/log.txt

# Preverify and obfuscate (ProGuard)
# Note: ProGuard 6.0.3 is the last version to run under JDK 6, as of writing, the latest version can run under JDK 8
echo "Verifying"
${JAVA_HOME}/bin/java -jar sdk/proguard.jar @midlets.pro
rm bin/in.jar
mv bin/out.jar "bin/${PROJECT_NAME}.jar"

# Create JAD file if jadmaker is available
if command -v jadmaker > /dev/null; then
    echo "Creating JAD"
    jadmaker --force "bin/${PROJECT_NAME}.jar"
fi

# Show output JAR file size
wc -c "bin/${PROJECT_NAME}.jar"