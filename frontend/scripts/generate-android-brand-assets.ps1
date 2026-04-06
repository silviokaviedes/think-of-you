Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = 'Stop'

function New-Color([int]$r, [int]$g, [int]$b, [int]$a = 255) {
    return [System.Drawing.Color]::FromArgb($a, $r, $g, $b)
}

function Add-RoundedRect([System.Drawing.Drawing2D.GraphicsPath]$path, [float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $diameter = $radius * 2
    $path.AddArc($x, $y, $diameter, $diameter, 180, 90)
    $path.AddArc($x + $width - $diameter, $y, $diameter, $diameter, 270, 90)
    $path.AddArc($x + $width - $diameter, $y + $height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($x, $y + $height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
}

function Draw-Heart([System.Drawing.Graphics]$graphics, [float]$centerX, [float]$centerY, [float]$size, [System.Drawing.Brush]$brush) {
    $half = $size / 2
    $left = [System.Drawing.RectangleF]::new($centerX - $half, $centerY - $half * 0.72, $half, $half)
    $right = [System.Drawing.RectangleF]::new($centerX, $centerY - $half * 0.72, $half, $half)
    $points = [System.Drawing.PointF[]]@(
        [System.Drawing.PointF]::new($centerX - $half * 0.95, $centerY - $half * 0.2),
        [System.Drawing.PointF]::new($centerX + $half * 0.95, $centerY - $half * 0.2),
        [System.Drawing.PointF]::new($centerX, $centerY + $half * 1.05)
    )

    $graphics.FillEllipse($brush, $left)
    $graphics.FillEllipse($brush, $right)
    $graphics.FillPolygon($brush, $points)
}

function Draw-ThoughtBubble([System.Drawing.Graphics]$graphics, [float]$width, [float]$height, [switch]$withShadow) {
    $bubbleBrush = [System.Drawing.SolidBrush]::new((New-Color 255 255 255))
    $shadowBrush = [System.Drawing.SolidBrush]::new((New-Color 18 38 67 38))
    $heartBrush = [System.Drawing.SolidBrush]::new((New-Color 239 91 98))

    $mainRects = @(
        [System.Drawing.RectangleF]::new($width * 0.21, $height * 0.20, $width * 0.48, $height * 0.42),
        [System.Drawing.RectangleF]::new($width * 0.46, $height * 0.16, $width * 0.28, $height * 0.29),
        [System.Drawing.RectangleF]::new($width * 0.34, $height * 0.14, $width * 0.26, $height * 0.23),
        [System.Drawing.RectangleF]::new($width * 0.55, $height * 0.28, $width * 0.16, $height * 0.16)
    )
    $tailRects = @(
        [System.Drawing.RectangleF]::new($width * 0.18, $height * 0.60, $width * 0.11, $height * 0.11),
        [System.Drawing.RectangleF]::new($width * 0.11, $height * 0.71, $width * 0.07, $height * 0.07)
    )

    if ($withShadow) {
        foreach ($rect in ($mainRects + $tailRects)) {
            $shadowRect = [System.Drawing.RectangleF]::new($rect.X + $width * 0.018, $rect.Y + $height * 0.022, $rect.Width, $rect.Height)
            $graphics.FillEllipse($shadowBrush, $shadowRect)
        }
    }

    foreach ($rect in ($mainRects + $tailRects)) {
        $graphics.FillEllipse($bubbleBrush, $rect)
    }

    Draw-Heart $graphics ($width * 0.45) ($height * 0.40) ($width * 0.19) $heartBrush

    $bubbleBrush.Dispose()
    $shadowBrush.Dispose()
    $heartBrush.Dispose()
}

function Save-Icon([string]$path, [int]$size) {
    $bitmap = [System.Drawing.Bitmap]::new($size, $size)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.Clear((New-Color 255 255 255 0))

    $backgroundRect = [System.Drawing.Rectangle]::new(0, 0, $size, $size)
    $backgroundBrush = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        [System.Drawing.Point]::new(0, 0),
        [System.Drawing.Point]::new($size, $size),
        (New-Color 250 126 90),
        (New-Color 244 84 126)
    )
    $backgroundBrush.SetBlendTriangularShape(0.55)

    $rounded = [System.Drawing.Drawing2D.GraphicsPath]::new()
    Add-RoundedRect $rounded ($size * 0.08) ($size * 0.08) ($size * 0.84) ($size * 0.84) ($size * 0.24)
    $graphics.FillPath($backgroundBrush, $rounded)

    $glowBrush = [System.Drawing.SolidBrush]::new((New-Color 255 255 255 35))
    $graphics.FillEllipse($glowBrush, $size * 0.17, $size * 0.12, $size * 0.42, $size * 0.25)

    Draw-ThoughtBubble $graphics ($size * 0.72) ($size * 0.72) -withShadow

    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)

    $glowBrush.Dispose()
    $rounded.Dispose()
    $backgroundBrush.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

function Save-Splash([string]$path, [int]$width, [int]$height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic

    $backgroundBrush = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        [System.Drawing.Point]::new(0, 0),
        [System.Drawing.Point]::new($width, $height),
        (New-Color 255 238 231),
        (New-Color 255 203 211)
    )
    $graphics.FillRectangle($backgroundBrush, 0, 0, $width, $height)

    $accentBrush = [System.Drawing.SolidBrush]::new((New-Color 255 255 255 70))
    $graphics.FillEllipse($accentBrush, $width * 0.06, $height * 0.08, $width * 0.28, $width * 0.28)
    $graphics.FillEllipse($accentBrush, $width * 0.70, $height * 0.18, $width * 0.18, $width * 0.18)
    $graphics.FillEllipse($accentBrush, $width * 0.76, $height * 0.72, $width * 0.22, $width * 0.22)

    $markSize = [Math]::Min($width, $height) * 0.34
    $markX = ($width - $markSize) / 2
    $markY = $height * 0.24
    $graphics.TranslateTransform($markX, $markY)
    Draw-ThoughtBubble $graphics $markSize $markSize -withShadow
    $graphics.ResetTransform()

    $titleFont = [System.Drawing.Font]::new('Segoe UI Semibold', [Math]::Max(28, [Math]::Round([Math]::Min($width, $height) * 0.065)), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $subtitleFont = [System.Drawing.Font]::new('Segoe UI', [Math]::Max(16, [Math]::Round([Math]::Min($width, $height) * 0.028)), [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $titleBrush = [System.Drawing.SolidBrush]::new((New-Color 82 34 47))
    $subtitleBrush = [System.Drawing.SolidBrush]::new((New-Color 121 72 84))
    $centerFormat = [System.Drawing.StringFormat]::new()
    $centerFormat.Alignment = [System.Drawing.StringAlignment]::Center

    $graphics.DrawString('Thinking of You', $titleFont, $titleBrush, $width / 2, $height * 0.62, $centerFormat)
    $graphics.DrawString('Quiet care, instantly shared.', $subtitleFont, $subtitleBrush, $width / 2, $height * 0.70, $centerFormat)

    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)

    $centerFormat.Dispose()
    $titleBrush.Dispose()
    $subtitleBrush.Dispose()
    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $accentBrush.Dispose()
    $backgroundBrush.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$resRoot = Join-Path $repoRoot 'android/app/src/main/res'

$iconSizes = @{
    'mipmap-mdpi' = 48
    'mipmap-hdpi' = 72
    'mipmap-xhdpi' = 96
    'mipmap-xxhdpi' = 144
    'mipmap-xxxhdpi' = 192
}

foreach ($entry in $iconSizes.GetEnumerator()) {
    $dir = Join-Path $resRoot $entry.Key
    Save-Icon (Join-Path $dir 'ic_launcher.png') $entry.Value
    Save-Icon (Join-Path $dir 'ic_launcher_round.png') $entry.Value
    Save-Icon (Join-Path $dir 'ic_launcher_foreground.png') $entry.Value
}

$splashSizes = @(
    @{ Folder = 'drawable-port-mdpi'; Width = 320; Height = 480 },
    @{ Folder = 'drawable-port-hdpi'; Width = 480; Height = 720 },
    @{ Folder = 'drawable-port-xhdpi'; Width = 720; Height = 960 },
    @{ Folder = 'drawable-port-xxhdpi'; Width = 960; Height = 1440 },
    @{ Folder = 'drawable-port-xxxhdpi'; Width = 1280; Height = 1920 },
    @{ Folder = 'drawable-land-mdpi'; Width = 480; Height = 320 },
    @{ Folder = 'drawable-land-hdpi'; Width = 720; Height = 480 },
    @{ Folder = 'drawable-land-xhdpi'; Width = 960; Height = 720 },
    @{ Folder = 'drawable-land-xxhdpi'; Width = 1440; Height = 960 },
    @{ Folder = 'drawable-land-xxxhdpi'; Width = 1920; Height = 1280 },
    @{ Folder = 'drawable'; Width = 1280; Height = 1920 }
)

foreach ($splash in $splashSizes) {
    $dir = Join-Path $resRoot $splash.Folder
    Save-Splash (Join-Path $dir 'splash.png') $splash.Width $splash.Height
}
