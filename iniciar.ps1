$MavenVersion = "3.9.5"
$MavenDir = "apache-maven-$MavenVersion"
$MavenZip = "maven.zip"
$MavenUrl = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
$MvnCmd = ".\$MavenDir\bin\mvn.cmd"

# Força o uso do TLS 1.2 (necessário em algumas versões do Windows)
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

# Verifica se o Maven já foi baixado
if (-not (Test-Path -Path $MvnCmd)) {
    Write-Host "Baixando o Maven para executar o projeto... (Isso acontece apenas na primeira vez)" -ForegroundColor Cyan
    Try {
        Invoke-WebRequest -Uri $MavenUrl -OutFile $MavenZip -UseBasicParsing
        Write-Host "Extraindo o Maven..." -ForegroundColor Cyan
        Expand-Archive -Path $MavenZip -DestinationPath "." -Force
        Remove-Item $MavenZip
    } Catch {
        Write-Host "Erro ao baixar o Maven. Verifique sua conexão com a internet." -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        Exit
    }
}

Write-Host "Maven pronto!" -ForegroundColor Green

Write-Host "======================================================" -ForegroundColor Yellow
Write-Host " INICIANDO O SERVIDOR E O SITE (BACKEND)              " -ForegroundColor Yellow
Write-Host "======================================================" -ForegroundColor Yellow

# Inicia o backend em background
Start-Process -NoNewWindow -FilePath $MvnCmd -ArgumentList "-pl api spring-boot:run"

Write-Host "Servidor sendo iniciado! Ele estará disponível em http://localhost:8081" -ForegroundColor Green
Write-Host "Aguardando 15 segundos para o servidor ligar completamente antes de abrir o aplicativo Desktop..." -ForegroundColor Cyan

Start-Sleep -Seconds 15

Write-Host "======================================================" -ForegroundColor Yellow
Write-Host " INICIANDO O APLICATIVO DESKTOP (JAVAFX)              " -ForegroundColor Yellow
Write-Host "======================================================" -ForegroundColor Yellow

# Inicia o frontend Desktop
& $MvnCmd -pl desktop clean javafx:run

Write-Host "Para fechar o servidor Spring Boot depois de fechar o Desktop, feche esta janela do terminal." -ForegroundColor Red
