$ErrorActionPreference = 'Stop'

$targetDirectory = 'C:\Program Files\Docker'
$imageDirectory = Join-Path $targetDirectory 'images'
$artifactDirectory = 'C:\code\AICodeGenerate\.codex-artifacts'
$screenshotDirectory = Join-Path $artifactDirectory 'redis-insight'

New-Item -ItemType Directory -Path $imageDirectory -Force | Out-Null
$guideTarget = Get-ChildItem -LiteralPath $targetDirectory -Filter '*.md' | Select-Object -First 1
if ($null -eq $guideTarget) {
    throw 'No existing Markdown guide was found in the Docker directory.'
}
Copy-Item -LiteralPath (Join-Path $artifactDirectory 'Docker-Redis-Guide.md') -Destination $guideTarget.FullName -Force
Copy-Item -LiteralPath (Join-Path $screenshotDirectory 'redis-insight-connection-settings.png') -Destination (Join-Path $imageDirectory 'redis-insight-connection-settings.png') -Force
Copy-Item -LiteralPath (Join-Path $screenshotDirectory 'redis-insight-browser.png') -Destination (Join-Path $imageDirectory 'redis-insight-browser.png') -Force
Copy-Item -LiteralPath (Join-Path $screenshotDirectory 'redis-insight-workbench.png') -Destination (Join-Path $imageDirectory 'redis-insight-workbench.png') -Force
