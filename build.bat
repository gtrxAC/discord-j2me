@echo off

:: Path to Java JDK. The JDK listed here must support targeting Java 1.2 (J2ME).
:: For example OpenJDK 8 (https://adoptium.net/temurin/releases/?version=8)
:: By default, the folder starting with "jdk" inside the sdk folder is used.
:: To use another folder, uncomment the next line and comment the one after it:
:: set JAVA_HOME=path\to\jdk
for /f "delims=" %%G in ('dir /b "sdk\jdk*"') do set JAVA_HOME="sdk\%%G"

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

if not exist sdk/node_modules (
    cd sdk
    npm i
    cd ..
)
node sdk/compile_all.js