# Warehouse Inventory System - Development Environment Startup Script

Write-Host "========================================" -ForegroundColor Green
Write-Host "Warehouse Inventory System - Dev Environment" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check Java
Write-Host "Checking Java environment..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java found: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found. Please install Java 17 or higher." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Maven
Write-Host "Checking Maven environment..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "Maven found: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Maven not found. Please install Maven." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Node.js
Write-Host "Checking Node.js environment..." -ForegroundColor Yellow
try {
    $nodeVersion = node -v
    Write-Host "Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Node.js not found. Please install Node.js 16 or higher." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Backend Service..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Start Backend
Set-Location backend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn spring-boot:run" -WindowStyle Normal

Write-Host "Waiting for backend service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Frontend Service..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Start Frontend
Set-Location ..\frontend
Start-Process powershell -ArgumentList "-NoExit", "-Command", "npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Services Started Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Backend Service: http://localhost:8080" -ForegroundColor White
Write-Host "Frontend Service: http://localhost:3000" -ForegroundColor White
Write-Host "Default Admin Account: admin / admin123" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green

Read-Host "Press Enter to exit"
