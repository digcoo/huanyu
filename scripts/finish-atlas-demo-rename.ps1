# 关闭微信开发者工具 / Cursor 对 atlas_wechat 的占用后执行：
#   cd d:\workspaces\huanyu
#   .\scripts\finish-atlas-demo-rename.ps1

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$old = Join-Path $root 'atlas_wechat'
$new = Join-Path $root 'atlas_demo'

if (-not (Test-Path $new)) {
  Write-Error 'atlas_demo not found. Run copy/rename from repo first.'
}

if (Test-Path $old) {
  Write-Host 'Removing old atlas_wechat ...'
  Remove-Item $old -Recurse -Force
  Write-Host 'Removed atlas_wechat.'
} else {
  Write-Host 'atlas_wechat already gone.'
}

Write-Host 'Done. Reopen WeChat DevTools with: ' $new
