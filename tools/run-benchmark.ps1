param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7")]
    [string]$Scenario,

    [Parameter(Mandatory = $true)]
    [ValidateSet("Begin", "Finish")]
    [string]$Phase,

    [ValidateRange(1, 99)]
    [int]$Run = 1,

    [ValidateRange(1, 180)]
    [int]$DurationMinutes = 10,

    [ValidateRange(0, 100)]
    [int]$BatteryEndPercent = -1,

    [string]$DeviceSerial,

    [switch]$RunTimer
)

$ErrorActionPreference = "Stop"
$resultDirectory = Join-Path $PSScriptRoot "..\benchmark-results"
$runPrefix = "$Scenario-run$Run"
$statePath = Join-Path $resultDirectory "$runPrefix.json"

function Get-AdbPath {
    $candidates = @()
    if ($env:ANDROID_SDK_ROOT) {
        $candidates += (Join-Path -Path $env:ANDROID_SDK_ROOT -ChildPath "platform-tools\adb.exe")
    }
    if ($env:ANDROID_HOME) {
        $candidates += (Join-Path -Path $env:ANDROID_HOME -ChildPath "platform-tools\adb.exe")
    }
    if ($env:LOCALAPPDATA) {
        $candidates += (Join-Path -Path $env:LOCALAPPDATA -ChildPath "Android\Sdk\platform-tools\adb.exe")
    }
    $candidates = @($candidates | Where-Object { Test-Path -LiteralPath $_ })

    if ($candidates.Count -eq 0) {
        throw "adb.exe nao encontrado. Instale Android SDK Platform-Tools ou defina ANDROID_SDK_ROOT."
    }
    return $candidates[0]
}

function Invoke-Adb {
    param([string[]]$Arguments)

    $args = @()
    if ($DeviceSerial) {
        $args += "-s"
        $args += $DeviceSerial
    }
    $args += $Arguments
    $output = & $script:adbPath @args 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "adb $($Arguments -join ' ') falhou: $output"
    }
    return $output
}

function Get-BatteryLevel {
    $battery = Invoke-Adb @("shell", "dumpsys", "battery")
    $match = [regex]::Match(($battery -join "`n"), "(?m)^\s*level:\s*(\d+)")
    if (!$match.Success) {
        throw "Nao foi possivel ler o nivel de bateria pelo adb."
    }
    return [int]$match.Groups[1].Value
}

function Save-AdbSnapshot {
    param([string]$Suffix)

    Invoke-Adb @("shell", "dumpsys", "battery") |
        Set-Content -Path (Join-Path $resultDirectory "$runPrefix-battery-$Suffix.txt")
    Invoke-Adb @("shell", "dumpsys", "thermalservice") |
        Set-Content -Path (Join-Path $resultDirectory "$runPrefix-thermal-$Suffix.txt")
    Invoke-Adb @("shell", "dumpsys", "meminfo", "com.meta.wearable.dat.externalsampleapps.cameraaccess") |
        Set-Content -Path (Join-Path $resultDirectory "$runPrefix-meminfo-$Suffix.txt")
}

$script:adbPath = Get-AdbPath
New-Item -ItemType Directory -Force $resultDirectory | Out-Null

if ($Phase -eq "Begin") {
    $deviceState = (Invoke-Adb @("get-state") | Select-Object -First 1).Trim()
    if ($deviceState -ne "device") {
        throw "O dispositivo ADB nao esta pronto. Estado atual: $deviceState"
    }

    Write-Host "Cenario $Scenario, rodada $Run. Configure o app agora e deixe o cenario ativo." -ForegroundColor Cyan
    Write-Host "Resetando batterystats antes de desconectar o USB..."
    Invoke-Adb @("shell", "dumpsys", "batterystats", "--reset") | Out-Null
    Save-AdbSnapshot "start"

    $startBattery = Read-Host "Desconecte o USB e digite o percentual exibido no celular para iniciar o cronometro"
    if ($startBattery -notmatch '^\d{1,3}$' -or [int]$startBattery -gt 100) {
        throw "Percentual de bateria invalido."
    }

    $state = [ordered]@{
        scenario = $Scenario
        run = $Run
        startedAtUtc = [DateTime]::UtcNow.ToString("o")
        startBatteryPercent = [int]$startBattery
        intendedDurationMinutes = $DurationMinutes
        adbDevice = if ($DeviceSerial) { $DeviceSerial } else { "default" }
    }
    $state | ConvertTo-Json | Set-Content -Path $statePath
    Write-Host "Benchmark iniciado sem USB. Resultado sera salvo em $resultDirectory" -ForegroundColor Green

    if ($RunTimer) {
        Write-Host "Aguarde $DurationMinutes minuto(s). Nao conecte o USB durante a rodada."
        Start-Sleep -Seconds ($DurationMinutes * 60)
        Write-Host "Tempo concluido. Anote a bateria final NO CELULAR antes de reconectar o cabo." -ForegroundColor Yellow
    }
    exit 0
}

if (!(Test-Path $statePath)) {
    throw "Nao existe inicio registrado para $runPrefix. Execute primeiro com -Phase Begin."
}
if ($BatteryEndPercent -lt 0) {
    throw "Informe -BatteryEndPercent com o valor anotado no celular antes de reconectar o USB."
}

$state = Get-Content -Raw $statePath | ConvertFrom-Json
$startedAt = [DateTime]::Parse($state.startedAtUtc).ToUniversalTime()
$elapsedMinutes = ([DateTime]::UtcNow - $startedAt).TotalMinutes
if ($elapsedMinutes -le 0) {
    throw "Duracao invalida."
}

Save-AdbSnapshot "finish"
Invoke-Adb @("shell", "dumpsys", "batterystats") |
    Set-Content -Path (Join-Path $resultDirectory "$runPrefix-batterystats.txt")

$drainPercent = [int]$state.startBatteryPercent - $BatteryEndPercent
$drainPerHour = [math]::Round(($drainPercent / $elapsedMinutes) * 60, 2)
$summary = [ordered]@{
    scenario = $state.scenario
    run = $state.run
    startedAtUtc = $state.startedAtUtc
    finishedAtUtc = [DateTime]::UtcNow.ToString("o")
    elapsedMinutes = [math]::Round($elapsedMinutes, 2)
    startBatteryPercent = $state.startBatteryPercent
    endBatteryPercent = $BatteryEndPercent
    drainPercent = $drainPercent
    estimatedDrainPercentPerHour = $drainPerHour
    intendedDurationMinutes = $state.intendedDurationMinutes
}
$summaryPath = Join-Path $resultDirectory "$runPrefix-summary.json"
$summary | ConvertTo-Json | Set-Content -Path $summaryPath

Write-Host "Resultado salvo em $summaryPath" -ForegroundColor Green
$summary | Format-List
