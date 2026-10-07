$ErrorActionPreference='Stop'
Set-Location (Join-Path (Split-Path $PSScriptRoot -Parent) 'frontend')
& npm.cmd ci
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
& npm.cmd run dev
