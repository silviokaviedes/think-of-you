param(
    [switch]$SkipOpen
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$frontendDir = Split-Path -Parent $PSScriptRoot

Push-Location $frontendDir
try {
    Write-Host 'Building Android app for local Docker Compose backend...'
    & npm run build:android-local
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Write-Host 'Syncing Capacitor Android project...'
    & npm run android:sync
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    if (-not $SkipOpen) {
        Write-Host 'Opening Android Studio project...'
        & npm run android:open
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
} finally {
    Pop-Location
}
