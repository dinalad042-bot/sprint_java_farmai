@echo off
REM FarmAI Database Setup Script for Windows
REM This script helps set up the database for manual testing

echo 🚀 FarmAI Database Setup Script
echo ══════════════════════════════════════
echo.

REM Check if MySQL is in PATH
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ MySQL client not found in PATH
    echo Please ensure MySQL is installed and accessible
    echo Common locations: C:\xampp\mysql\bin or C:\Program Files\MySQL\MySQL Server X.X\bin
    pause
    exit /b 1
)

REM Test MySQL connection
echo 🔌 Testing MySQL connection...
mysql -u root -e "SELECT 1;" 2>nul
if %errorlevel% equ 0 (
    echo ✅ MySQL connection successful
) else (
    echo ❌ MySQL connection failed
    echo Please check MySQL credentials and ensure it's running
    pause
    exit /b 1
)

REM Create database if it doesn't exist
echo.
echo 📁 Creating database...
mysql -u root -e "CREATE DATABASE IF NOT EXISTS farmai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo ✅ Database 'farmai' created/verified

REM Run the SQL script
echo.
echo 📋 Setting up database schema...
mysql -u root farmai < ..\database\farmai_complete.sql
if %errorlevel% equ 0 (
    echo ✅ Database schema setup completed
) else (
    echo ❌ Database schema setup failed
    pause
    exit /b 1
)

REM Verify the setup
echo.
echo 📊 Verifying database setup...
for /f "tokens=*" %%i in ('mysql -u root farmai -e "SELECT COUNT(*) FROM user;" -B -N') do set USER_COUNT=%%i
echo ✅ Found %USER_COUNT% users in database

for /f "tokens=*" %%i in ('mysql -u root farmai -e "SELECT COUNT(*) FROM ferme;" -B -N') do set FARM_COUNT=%%i
echo ✅ Found %FARM_COUNT% farms in database

echo.
echo 🎉 Database setup completed successfully!
echo.
echo 🔑 Test login credentials:
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━
echo Admin: admin@farmai.tn / password123
echo Expert: expert@farmai.tn / password123
echo Agricole: agricole@farmai.tn / password123
echo Fournisseur: fournisseur@farmai.tn / password123
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.
echo 🎮 You can now start the FarmAI application!
echo Run: mvn javafx:run
pause