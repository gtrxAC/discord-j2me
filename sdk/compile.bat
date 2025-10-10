@echo off
setlocal enabledelayedexpansion

:: Create directories if they don't already exist, and remove any leftover files.
echo Setting up directories
mkdir bin classes res src build 2>nul
del /q build\*
del /q classes\*

:: Recursively find java files that need to be preprocessed (src)
set JAVA_FILES=
for /r src %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

:: Preprocess Java source files (src -> build/src)
echo Preprocessing
node sdk\preprocess.js !JAVA_FILES! manifest.mf midlets.pro %DEFINES%

:: Recursively find java files that need to be compiled (build/src)
set JAVA_FILES=
for /r build\src %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

:: Compile for Java 1.2. Most (but not all) J2ME devices support Java 1.3
:: If you want to change the APIs (bootclasspath), also edit the ProGuard config (midlets.pro)
echo Compiling
%JAVA_HOME%\bin\javac -d classes -source 1.2 -target 1.2 -Xlint:-options -encoding UTF-8 -bootclasspath %BOOTCLASSPATH% !JAVA_FILES! > sdk\log.txt

:: Check for compilation errors
if errorlevel 1 (
    exit /b !EXIT_CODE!
)

echo TODO: extract modernconnector jar - not sure, this script might be broken anyway

echo Creating JAR
%JAVA_HOME%\bin\jar cvfm bin\in.jar build\manifest.mf -C classes . -C res . >> sdk\log.txt

:: Preverify and obfuscate (ProGuard)
:: Note: ProGuard 6.0.3 is the last version to run under JDK 6, as of writing, the latest version can run under JDK 8
echo Verifying
%JAVA_HOME%\bin\java -jar sdk\proguard.jar @build/midlets.pro -printmapping "bin/%JAR_NAME%.map"
del bin\in.jar
move bin\out.jar "bin\%JAR_NAME%.jar"

:: Create JAD file if jadmaker is available
where jadmaker >nul 2>nul
if not errorlevel 1 (
    echo Creating JAD
    jadmaker --force "bin\%JAR_NAME%.jar"
)

endlocal
