param(
    [switch]$SkipOpen
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$frontendDir = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $frontendDir '.env.android-railway.local'

if (-not (Test-Path $envFile)) {
    throw "Missing $envFile. Create it first with VITE_API_BASE_URL=https://think-of-you-production.up.railway.app"
}

Push-Location $frontendDir
try {
    Write-Host 'Building Android app for Railway backend...'
    & npm run build:android-railway
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
