# Stop on error
$ErrorActionPreference = "Stop"

# Clean up JAVA_HOME path in case it has quotes
$env:JAVA_HOME = $env:JAVA_HOME.Trim('"')

Write-Host "Setting up directories"
# Create directories if they don't exist
$dirs = @("bin", "classes", "res", "src", "build")
foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
}

# Clean up build and classes
Remove-Item -Recurse -Force build\* -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force classes\* -ErrorAction SilentlyContinue

Write-Host "Preprocessing"
# Find all .java files in src
$javaFiles = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
# Run preprocessing
node sdk/preprocess.js @javaFiles manifest.mf midlets.pro $env:DEFINES

Write-Host "Copying assets"
New-Item -ItemType Directory -Path build\res -Force | Out-Null
Copy-Item res\* build\res -Recurse -Force

# Remove excluded files, if any
if ($env:EXCLUDES) {
    # Split EXCLUDES by spaces
    $excludes = $env:EXCLUDES -split '[ ]+' | Where-Object { $_ -ne "" }

    Set-Location build\res
    foreach ($file in $excludes) {
        Remove-Item -Recurse -Force $file -ErrorAction SilentlyContinue
    }
    Set-Location ../..
}

Write-Host "Compiling"
$javac = Join-Path $env:JAVA_HOME "bin\javac"
$javaFilesToCompile = Get-ChildItem -Recurse -Path build\src -Filter *.java | ForEach-Object { $_.FullName }
& $javac @javaFilesToCompile `
    -d classes `
    -source 1.2 `
    -target 1.2 `
    -nowarn `
    -encoding UTF-8 `
    -bootclasspath $env:BOOTCLASSPATH `
    *> sdk/log.txt
# Note: use -nowarn to suppress all warnings, because deprecation warning is somehow treated as an error

Write-Host "Creating JAR"
$jar = Join-Path $env:JAVA_HOME "bin\jar"
& $jar cvfm bin/in.jar build/manifest.mf -C classes . -C build/res . >> sdk/log.txt

Write-Host "Verifying"
$java = Join-Path $env:JAVA_HOME "bin\java"
& $java -jar sdk/proguard.jar @build/midlets.pro -printmapping "bin/$($env:JAR_NAME).map"
Remove-Item bin/in.jar
Move-Item bin/out.jar "bin/$($env:JAR_NAME).jar" -Force

# Create JAD file if jadmaker is available
if (Get-Command jadmaker -ErrorAction SilentlyContinue) {
    Write-Host "Creating JAD"
    jadmaker --force "bin/$($env:JAR_NAME).jar"
}

# Show output JAR file size
$jarPath = "bin/$($env:JAR_NAME).jar"
(Get-Item $jarPath).Length
