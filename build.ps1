$ErrorActionPreference = "Stop"

Write-Host "========================================"
Write-Host " MyApp Build Script"
Write-Host "========================================"
Write-Host ""
Write-Host " 1. compile   - Kotlin compile only (fast)"
Write-Host " 2. assemble  - Full APK build"
Write-Host " 3. clean     - Clean + full rebuild"
Write-Host " 4. install   - Build + install to device"
Write-Host " 5. run       - Build + install + launch app"
Write-Host ""

$choice = Read-Host "Select (1-5)"

function Check-Device {
    $devices = adb devices 2>$null | Select-String "device$"
    if ($devices) { return $true }

    Write-Host ""
    Write-Host "========================================"
    Write-Host " No connected device found!"
    Write-Host "========================================"
    Write-Host ""

    $avds = & "$env:ANDROID_HOME\emulator\emulator" -list-avds 2>$null
    if (-not $avds) {
        Write-Host " No emulator images found."
        Write-Host " Create one in Android Studio: AVD Manager"
        return $false
    }

    $avdList = @($avds)
    for ($i = 0; $i -lt $avdList.Count; $i++) {
        Write-Host "  $($i+1). $($avdList[$i])"
    }

    Write-Host ""
    if ($avdList.Count -eq 1) {
        $start = Read-Host "Start '$($avdList[0])' and continue? (y/n)"
        if ($start -ne 'y') { return $false }
        Launch-Emu $avdList[0]
    } else {
        $pick = Read-Host "Enter number to start (1-$($avdList.Count))"
        $idx = [int]$pick - 1
        if ($idx -ge 0 -and $idx -lt $avdList.Count) {
            Launch-Emu $avdList[$idx]
        } else {
            return $false
        }
    }
    return $true
}

function Launch-Emu($name) {
    Write-Host ""
    Write-Host "Starting $name..."
    Start-Process "$env:ANDROID_HOME\emulator\emulator" -ArgumentList "-avd", $name -WindowStyle Hidden
    Write-Host "Waiting for device to boot (this may take 30-60s)..."
    adb wait-for-device 2>$null
    do {
        Start-Sleep -Seconds 2
        $boot = (adb shell getprop sys.boot_completed 2>$null).Trim()
    } while ($boot -ne "1")
    Write-Host "Emulator is ready."
}

switch ($choice) {
    "1" {
        Write-Host "Compiling Kotlin..."
        & .\gradlew.bat compileDebugKotlin
    }
    "2" {
        Write-Host "Building APK..."
        & .\gradlew.bat assembleDebug
    }
    "3" {
        Write-Host "Clean + rebuild..."
        & .\gradlew.bat clean assembleDebug
    }
    { $_ -eq "4" -or $_ -eq "5" } {
        if (-not (Check-Device)) { break }

        if ($choice -eq "4") {
            Write-Host "Build + install..."
            & .\gradlew.bat installDebug
        }
        if ($choice -eq "5") {
            Write-Host "Build + install + launch..."
            & .\gradlew.bat installDebug
            if ($LASTEXITCODE -eq 0) {
                adb shell am start -n com.example.myapplication/.MainActivity
            }
        }
    }
    default {
        Write-Host "Invalid choice"
    }
}

Read-Host "Press Enter to exit"
