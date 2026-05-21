param(
    [int]$VersionCode,
    [string]$VersionName,
    [switch]$SkipAssetGeneration
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$frontendDir = Split-Path -Parent $PSScriptRoot
$androidDir = Join-Path $frontendDir 'android'
$buildGradle = Join-Path $androidDir 'app/build.gradle'
$envFile = Join-Path $frontendDir '.env.android-railway.local'

function Invoke-CheckedCommand([string]$Label, [scriptblock]$Command) {
    Write-Host $Label
    & $Command
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

if (-not (Test-Path $envFile)) {
    throw "Missing $envFile. Create it first with VITE_API_BASE_URL=https://think-of-you-production.up.railway.app"
}

if (-not (Test-Path $buildGradle)) {
    throw "Missing $buildGradle"
}

$gradleContent = [System.IO.File]::ReadAllText($buildGradle)
$versionCodeMatch = [regex]::Match($gradleContent, '(?m)^(\s*versionCode\s+)(\d+)(\s*)$')
if (-not $versionCodeMatch.Success) {
    throw "Could not find versionCode in $buildGradle"
}

$currentVersionCode = [int]$versionCodeMatch.Groups[2].Value
$nextVersionCode = if ($PSBoundParameters.ContainsKey('VersionCode')) { $VersionCode } else { $currentVersionCode + 1 }

if ($nextVersionCode -le $currentVersionCode) {
    throw "New versionCode ($nextVersionCode) must be greater than current versionCode ($currentVersionCode)."
}

$updatedGradleContent = [regex]::Replace(
    $gradleContent,
    '(?m)^(\s*versionCode\s+)(\d+)(\s*)$',
    "`${1}$nextVersionCode`${3}",
    1
)

if ($PSBoundParameters.ContainsKey('VersionName')) {
    if (-not ([regex]::IsMatch($updatedGradleContent, '(?m)^(\s*versionName\s+")([^"]+)(".*)$'))) {
        throw "Could not find versionName in $buildGradle"
    }

    $updatedGradleContent = [regex]::Replace(
        $updatedGradleContent,
        '(?m)^(\s*versionName\s+")([^"]+)(".*)$',
        "`${1}$VersionName`${3}",
        1
    )
}

[System.IO.File]::WriteAllText($buildGradle, $updatedGradleContent, [System.Text.UTF8Encoding]::new($false))

Write-Host "Updated Android versionCode: $currentVersionCode -> $nextVersionCode"
if ($PSBoundParameters.ContainsKey('VersionName')) {
    Write-Host "Updated Android versionName: $VersionName"
}

Push-Location $frontendDir
try {
    if (-not $SkipAssetGeneration) {
        Invoke-CheckedCommand 'Regenerating Android launcher icons and splash assets...' {
            & powershell -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'generate-android-brand-assets.ps1')
        }
    }

    Invoke-CheckedCommand 'Building Android web assets for Railway backend...' {
        & npm run build:android-railway
    }

    Invoke-CheckedCommand 'Syncing Capacitor Android project...' {
        & npm run android:sync
    }

    Push-Location $androidDir
    try {
        Invoke-CheckedCommand 'Building signed Android App Bundle...' {
            & .\gradlew.bat bundleRelease
        }
    } finally {
        Pop-Location
    }
} finally {
    Pop-Location
}

Write-Host "Release bundle ready: $(Join-Path $androidDir 'app/build/outputs/bundle/release/app-release.aab')"
