@echo off
setlocal enabledelayedexpansion

:: Path to Java JDK. The JDK listed here must support targeting Java 1.2 (J2ME).
:: For example OpenJDK 8 (https://adoptium.net/temurin/releases/?version=8) or JDK 1.6.0_45
:: If the JDK in your PATH supports targeting Java 1.2, you can leave this blank.
set JAVA_HOME=sdk\jdk8u422-b05

:: Project name, used as output JAR file name. By default, this is the name of the current directory.
for %%F in (.) do set PROJECT_NAME=%%~nxF

:: Create directories if they don't already exist, and remove any leftover files.
echo Setting up directories
mkdir bin classes res src 2>nul
del /q classes\*

:: Generate language files
where node >nul 2>nul
if not errorlevel 1 (
    echo Generating translations
    cd language
    if not exist node_modules (
        npm i
    )
    node convert.js
    cd ..
)

:: Recursively find java files that need to be compiled
set JAVA_FILES=
for /r src %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

:: Compile for Java 1.2. Most (but not all) J2ME devices support Java 1.3
:: If you want to change the APIs (bootclasspath), also edit the ProGuard config (midlets.pro)
echo Compiling
%JAVA_HOME%\bin\javac -d classes -source 1.2 -target 1.2 -Xlint:-options -encoding UTF-8 -bootclasspath sdk\lib\jsr75.jar;sdk\lib\midpapi20.jar;sdk\lib\cldcapi10.jar !JAVA_FILES! > sdk\log.txt

:: Check for compilation errors
if errorlevel 1 (
    exit /b !EXIT_CODE!
)

echo Creating JAR
%JAVA_HOME%\bin\jar cvfm bin\in.jar manifest.mf -C classes . -C res . >> sdk\log.txt

:: Preverify and obfuscate (ProGuard)
:: Note: ProGuard 6.0.3 is the last version to run under JDK 6, as of writing, the latest version can run under JDK 8
echo Verifying
%JAVA_HOME%\bin\java -jar sdk\proguard.jar @midlets.pro
del bin\in.jar
move bin\out.jar "bin\%PROJECT_NAME%.jar"

:: Create JAD file if jadmaker is available
where jadmaker >nul 2>nul
if not errorlevel 1 (
    echo Creating JAD
    jadmaker --force "bin\%PROJECT_NAME%.jar"
)

endlocal
