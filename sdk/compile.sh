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
node sdk/preprocess.js `find src -name '*'.java` manifest.mf midlets.pro ${DEFINES}

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

if [[ ! -e lib/bouncycastle ]]; then
    echo "Extracting libraries"
    mkdir lib/bouncycastle
    cd lib/bouncycastle
    ${JAVA_HOME}/bin/jar xf ../bouncycastle.jar
    cd ../..
fi

echo "Creating JAR"
${JAVA_HOME}/bin/jar cvf bin/in.jar -C classes . -C build/res . >> sdk/log.txt
[[ ${MODCON} == 1 ]] && ${JAVA_HOME}/bin/jar uvf bin/in.jar -C lib/bouncycastle . >> sdk/log.txt
${JAVA_HOME}/bin/jar uvfm bin/in.jar build/manifest.mf >> sdk/log.txt

# Get size of previous files
PREV_ZIP_SIZE=$((wc -c "bin/${JAR_NAME}.jar" 2> /dev/null | grep -Eo "^[0-9]+") || echo 0)
PREV_UNZIP_SIZE=$((unzip -l bin/${JAR_NAME}.jar 2> /dev/null | grep -E "^\\s+([0-9]+).*?files$" | grep -Eo "^\\s+[0-9]+" | grep -Eo "[0-9]+") || echo 0)

# Preverify and obfuscate (ProGuard)
# Note: ProGuard 6.0.3 is the last version to run under JDK 6, as of writing, the latest version can run under JDK 8
echo "Verifying"
${JAVA_HOME}/bin/java -jar sdk/proguard.jar @build/midlets.pro -printmapping "bin/${JAR_NAME}.map"
rm bin/in.jar
mv bin/out.jar "bin/${JAR_NAME}.jar"

# Show output JAR file size
ZIP_SIZE=$((wc -c "bin/${JAR_NAME}.jar" 2> /dev/null | grep -Eo "^[0-9]+") || echo 0)
ZIP_DIFF=$[ $ZIP_SIZE - $PREV_ZIP_SIZE ]
[[ $ZIP_DIFF -gt 0 ]] && ZIP_DIFF="+$ZIP_DIFF"

UNZIP_SIZE=$((unzip -l bin/${JAR_NAME}.jar 2> /dev/null | grep -E "^\\s+([0-9]+).*?files$" | grep -Eo "^\\s+[0-9]+" | grep -Eo "[0-9]+") || echo 0)
UNZIP_DIFF=$[ $UNZIP_SIZE - $PREV_UNZIP_SIZE ]
[[ $UNZIP_DIFF -gt 0 ]] && UNZIP_DIFF="+$UNZIP_DIFF"

echo $ZIP_SIZE "($ZIP_DIFF)" "bin/${JAR_NAME}.jar"
echo $UNZIP_SIZE "($UNZIP_DIFF)" uncompressed