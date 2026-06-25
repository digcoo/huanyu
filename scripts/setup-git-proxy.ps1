# 为当前仓库启用 GitHub 代理（Git + gh CLI）
# 用法：.\scripts\setup-git-proxy.ps1

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$proxyFile = Join-Path $root '.gitconfig.proxy'
if (-not (Test-Path $proxyFile)) {
  Write-Error ".gitconfig.proxy not found at $proxyFile"
}

$proxyUrl = 'http://127.0.0.1:8890'
$currentInclude = git config --local --get include.path 2>$null
if ($currentInclude -ne '../.gitconfig.proxy') {
  git config --local include.path ../.gitconfig.proxy
  Write-Host "Configured: git config --local include.path ../.gitconfig.proxy"
} else {
  Write-Host 'Git local include.path already set.'
}

$env:HTTP_PROXY = $proxyUrl
$env:HTTPS_PROXY = $proxyUrl
$env:ALL_PROXY = $proxyUrl
[Environment]::SetEnvironmentVariable('HTTP_PROXY', $proxyUrl, 'User')
[Environment]::SetEnvironmentVariable('HTTPS_PROXY', $proxyUrl, 'User')
[Environment]::SetEnvironmentVariable('ALL_PROXY', $proxyUrl, 'User')
Write-Host "Set user env: HTTP_PROXY / HTTPS_PROXY / ALL_PROXY = $proxyUrl"

Write-Host ''
Write-Host 'Verify GitHub connectivity:'
git ls-remote origin HEAD 2>&1 | Select-Object -First 1
