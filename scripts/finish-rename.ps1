# 关闭 Cursor / 微信开发者工具后，在 PowerShell 中执行：
#   cd d:\workspaces\huanyu
#   .\scripts\finish-rename.ps1

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

if (Test-Path (Join-Path $root 'wechat_project')) {
  Write-Host 'Removing old wechat_project ...'
  Remove-Item (Join-Path $root 'wechat_project') -Recurse -Force
}

if (-not (Test-Path (Join-Path $root 'atlas_demo'))) {
  Write-Error 'atlas_demo not found. Copy may have failed.'
}

$parent = Split-Path -Parent $root
$target = Join-Path $parent 'atlas'
if ($root -ne $target) {
  if (Test-Path $target) {
    Write-Error "Target already exists: $target"
  }
  Write-Host "Renaming $(Split-Path -Leaf $root) -> atlas ..."
  Rename-Item -Path $root -NewName 'atlas'
  Write-Host "Done. Reopen workspace: $target"
} else {
  Write-Host 'Root is already atlas.'
}
