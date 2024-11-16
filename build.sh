#!/bin/bash

# Path to Java JDK. The JDK listed here must support targeting Java 1.2 (J2ME).
# For example OpenJDK 8 (https://adoptium.net/temurin/releases/?version=8)
export JAVA_HOME=sdk/jdk*

if ! command -v node > /dev/null; then
  echo "Please install Node.js"
  exit 1
fi

echo "Generating translations"
cd language
[[ -e node_modules ]] || npm i
node convert.js
cd ..

node sdk/compile_all.js