# 在代理环境下执行任意命令（供 Agent / 终端一键使用）
# 用法：.\scripts\with-proxy.ps1 git push
#       .\scripts\with-proxy.ps1 gh pr list

param(
  [string]$ProxyUrl = 'http://127.0.0.1:8890',
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$Command
)

if (-not $Command -or $Command.Count -eq 0) {
  Write-Error 'Usage: .\scripts\with-proxy.ps1 <command...>'
}

$env:HTTP_PROXY = $ProxyUrl
$env:HTTPS_PROXY = $ProxyUrl
$env:ALL_PROXY = $ProxyUrl
$env:NO_PROXY = 'localhost,127.0.0.1'

$exe = $Command[0]
$args = @()
if ($Command.Count -gt 1) {
  $args = $Command[1..($Command.Count - 1)]
}

& $exe @args
exit $LASTEXITCODE
