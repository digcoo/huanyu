# 为当前仓库启用 GitHub 代理（Git + gh CLI）
# 用法：.\scripts\setup-git-proxy.ps1 [proxyUrl]
# 默认：http://127.0.0.1:8890（Clash / V2Ray 系统代理）

param(
  [string]$ProxyUrl = 'http://127.0.0.1:8890'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$proxyFile = Join-Path $root '.gitconfig.proxy'
if (-not (Test-Path $proxyFile)) {
  Write-Error ".gitconfig.proxy not found at $proxyFile"
}

# 1) 直接写入 .git/config（Agent 沙箱内 include.path 可能不生效）
git config --local http.proxy $ProxyUrl
git config --local https.proxy $ProxyUrl
git config --local http.https://github.com.proxy $ProxyUrl
Write-Host "Configured git local: http.proxy / https.proxy / http.https://github.com.proxy = $ProxyUrl"

# 2) 保留 include.path，便于与 .gitconfig.proxy 模板同步（非沙箱环境双保险）
$currentInclude = git config --local --get include.path 2>$null
if ($currentInclude -ne '../.gitconfig.proxy') {
  git config --local include.path ../.gitconfig.proxy
  Write-Host 'Configured: git config --local include.path ../.gitconfig.proxy'
}

# 3) gh CLI 与部分工具只认环境变量
$env:HTTP_PROXY = $ProxyUrl
$env:HTTPS_PROXY = $ProxyUrl
$env:ALL_PROXY = $ProxyUrl
$env:NO_PROXY = 'localhost,127.0.0.1'
[Environment]::SetEnvironmentVariable('HTTP_PROXY', $ProxyUrl, 'User')
[Environment]::SetEnvironmentVariable('HTTPS_PROXY', $ProxyUrl, 'User')
[Environment]::SetEnvironmentVariable('ALL_PROXY', $ProxyUrl, 'User')
[Environment]::SetEnvironmentVariable('NO_PROXY', 'localhost,127.0.0.1', 'User')
Write-Host "Set user env: HTTP_PROXY / HTTPS_PROXY / ALL_PROXY = $ProxyUrl"

Write-Host ''
Write-Host 'Verify GitHub connectivity:'
git ls-remote origin HEAD 2>&1 | Select-Object -First 1
