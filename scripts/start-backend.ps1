param([switch]$SkipMigration)
$ErrorActionPreference='Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:DB_PASSWORD) {
 $safePassword=Read-Host 'MySQL password (not saved)' -AsSecureString
 $safePtr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($safePassword)
 try {$env:DB_PASSWORD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($safePtr)} finally {[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($safePtr)}
}
if (-not $env:JWT_SECRET) {
 $safeBytes=New-Object byte[] 48
 $safeRng=[Security.Cryptography.RandomNumberGenerator]::Create()
 try {$safeRng.GetBytes($safeBytes)} finally {$safeRng.Dispose()}
 $env:JWT_SECRET=[Convert]::ToBase64String($safeBytes)
 Write-Host 'Using an ephemeral signing key. Restarting invalidates previous demo sessions.'
}
if (-not $env:DB_USERNAME) {$env:DB_USERNAME='root'}
if (-not $SkipMigration) {
 $safeMysql=Get-Command mysql -ErrorAction SilentlyContinue
 if (-not $safeMysql) {$safeMysql=Get-Item 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -ErrorAction SilentlyContinue}
 if (-not $safeMysql) {throw 'mysql client not found; apply database/002_mfa_security.sql manually and pass -SkipMigration.'}
 $env:MYSQL_PWD=$env:DB_PASSWORD
 try {Get-Content database/002_mfa_security.sql -Raw | & $safeMysql.Source "--user=$env:DB_USERNAME" pam_database; if($LASTEXITCODE -ne 0){throw 'Database migration failed'}}
 finally {Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue}
}
& .\mvnw.cmd "-Dmaven.repo.local=$env:USERPROFILE\.m2\repository" spring-boot:run '-Dspring-boot.run.profiles=dev'
exit $LASTEXITCODE
