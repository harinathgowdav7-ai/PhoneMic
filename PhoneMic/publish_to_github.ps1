<#
.SYNOPSIS
    Creates the public 'PhoneMic' GitHub repository, pushes all commits, tags v1.0.0,
    and optionally builds the Windows installer & Android APK locally if SDKs are installed.
#>
param(
    [string]$RepoName = "PhoneMic"
)

$ErrorActionPreference = "Stop"
Write-Host "=== PhoneMic – GitHub Publish & Release Automation ===" -ForegroundColor Green

# 1. Verify Git & GitHub CLI (gh)
if (Get-Command gh -ErrorAction SilentlyContinue) {
    Write-Host "[1/3] Creating public repository '$RepoName' on your GitHub account..." -ForegroundColor Cyan
    gh repo create $RepoName --public --source=. --remote=origin --push
    git tag -f v1.0.0
    git push origin v1.0.0 --force
    Write-Host "Repository pushed! GitHub Actions is now compiling PhoneMic-v1.0.0.apk and PhoneMic-Windows-Setup-v1.0.0.exe." -ForegroundColor Green
} else {
    Write-Host "GitHub CLI ('gh') not found. Please add your GitHub remote URL and push:" -ForegroundColor Yellow
    Write-Host "  git remote add origin https://github.com/<YOUR_USERNAME>/PhoneMic.git"
    Write-Host "  git push -u origin main --tags"
}

# 2. Local Windows Build if .NET 8 SDK is installed
if (Get-Command dotnet -ErrorAction SilentlyContinue) {
    Write-Host "[2/3] Building Windows WPF Application locally..." -ForegroundColor Cyan
    Push-Location "$PSScriptRoot\windows"
    dotnet publish PhoneMic.Desktop/PhoneMic.Desktop.csproj -c Release -r win-x64 --self-contained true
    Pop-Location
}
