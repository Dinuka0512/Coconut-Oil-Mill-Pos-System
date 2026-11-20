<#
  Builds the Dilshan Oil Mill Windows setup EXE.

  The resulting "Dilshan Oil Mill Setup.exe" checks the target machine
  (Windows bitness, disk space, MySQL service/port) and then launches the
  bundled jpackage application installer. Java 21 and the Bebas Neue font
  are bundled inside the application installer.
#>
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$javaHome = 'C:\Program Files\Java\jdk-21.0.10'
$env:JAVA_HOME = $javaHome
$jpackage = Join-Path $javaHome 'bin\jpackage.exe'
$csc = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'

Write-Host '1/4 Building application JAR...'
& (Join-Path $root 'mvnw.cmd') -q -DskipTests package
if ($LASTEXITCODE -ne 0) { throw 'Maven package failed.' }

Write-Host '2/4 Preparing application installer input...'
$inputDir = Join-Path $root 'installer-build\app-input'
$outputDir = Join-Path $root 'installer-build\output'
New-Item -ItemType Directory -Force -Path $inputDir, $outputDir | Out-Null
Copy-Item (Join-Path $root 'target\KumaraCoconutOil-1.0-SNAPSHOT.jar') `
    (Join-Path $inputDir 'KumaraCoconutOil.jar') -Force

Write-Host '3/4 Building application installer EXE...'
Get-ChildItem $outputDir -File | Remove-Item -Force
& $jpackage `
    --type exe `
    --name 'Dilshan oil mill' `
    --app-version '1.0' `
    --input $inputDir `
    --main-jar 'KumaraCoconutOil.jar' `
    --main-class 'com.dev09.kumaracoconutoil.Main' `
    --runtime-image (Join-Path $root 'runtime') `
    --icon (Join-Path $root 'src\main\resources\asets\logo.ico') `
    --dest $outputDir `
    --description 'POS System - Developed by Dev09Solutions.com' `
    --vendor 'Dev09Solutions.com' `
    --copyright '2026 Dev09Solutions.com' `
    --win-per-user-install `
    --win-menu `
    --win-menu-group 'Dilshan Oil Mill' `
    --win-shortcut
if ($LASTEXITCODE -ne 0) { throw 'jpackage failed.' }

$appInstaller = Get-ChildItem $outputDir -Filter '*.exe' | Select-Object -First 1
if ($null -eq $appInstaller) { throw 'jpackage did not produce an EXE.' }
Copy-Item $appInstaller.FullName (Join-Path $root 'dist\Dilshan oil mill-1.0.exe') -Force

Write-Host '4/4 Compiling prerequisite-checking setup EXE...'
$setupExe = Join-Path $root 'dist\Dilshan Oil Mill Setup.exe'
$icon = Join-Path $root 'src\main\resources\asets\logo.ico'
$source = Join-Path $root 'installer\Setup.cs'
& $csc /nologo /target:winexe /out:$setupExe `
    /r:System.dll /r:System.Windows.Forms.dll /r:System.Drawing.dll /r:System.ServiceProcess.dll `
    /win32icon:$icon `
    "/resource:$($appInstaller.FullName),appinstaller" `
    $source
if ($LASTEXITCODE -ne 0) { throw 'C# compiler failed.' }

Write-Host "Done: $setupExe"
Get-Item $setupExe | Select-Object FullName, Length
