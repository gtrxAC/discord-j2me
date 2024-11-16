@echo off

:: Path to Java JDK. The JDK listed here must support targeting Java 1.2 (J2ME).
:: For example OpenJDK 8 (https://adoptium.net/temurin/releases/?version=8)
set JAVA_HOME=sdk\jdk*

where node >nul 2>nul
if errorlevel 1 (
    echo Please install Node.js
    exit 1
)

echo Generating translations
cd language
if not exist node_modules (
    npm i
)
node convert.js
cd ..

node sdk/compile_all.js