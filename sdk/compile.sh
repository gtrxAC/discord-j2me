#!/bin/bash

# Stop building if an error occurs
set -e

# Create directories if they don't already exist, and remove any leftover files.
echo "Setting up directories"
mkdir -p bin classes res src build
rm -rf build/*
rm -rf classes/*

# Preprocess Java source files
echo "Preprocessing"
node sdk/preprocess.js `find src -name '*'.java` manifest.mf ${DEFINES}

echo "Copying assets"
mkdir -p build/res
cp res/* build/res
cd build/res
rm -rf ${EXCLUDES}
cd ../..

# Compile for Java 1.2. Most (but not all) J2ME devices support Java 1.3
# If you want to change the APIs (bootclasspath), also edit the ProGuard config (midlets.pro)
echo "Compiling"
${JAVA_HOME}/bin/javac `find build/src -name '*'.java` -d classes \
    -source 1.2 -target 1.2 -Xlint:-options \
    -bootclasspath ${BOOTCLASSPATH} \
    > sdk/log.txt

echo "Creating JAR"
${JAVA_HOME}/bin/jar cvfm bin/in.jar build/manifest.mf -C classes . -C build/res . >> sdk/log.txt

# Preverify and obfuscate (ProGuard)
# Note: ProGuard 6.0.3 is the last version to run under JDK 6, as of writing, the latest version can run under JDK 8
echo "Verifying"
${JAVA_HOME}/bin/java -jar sdk/proguard.jar @midlets.pro -printmapping "bin/${JAR_NAME}.map"
rm bin/in.jar
mv bin/out.jar "bin/${JAR_NAME}.jar"

# Create JAD file if jadmaker is available
if command -v jadmaker > /dev/null; then
    echo "Creating JAD"
    jadmaker --force "bin/${JAR_NAME}.jar"
fi

# Show output JAR file size
wc -c "bin/${JAR_NAME}.jar"
