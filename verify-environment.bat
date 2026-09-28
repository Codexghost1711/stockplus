@echo off
echo StockPulse Environment Verification Script
echo ========================================
echo.

echo Checking Java...
java -version >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ Java is installed
    java -version 2>&1
) else (
    echo ✗ Java is not installed or not in PATH
)

echo.
echo Checking Java Compiler...
javac -version >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ Java Compiler (javac) is available
    javac -version 2>&1
) else (
    echo ✗ Java Compiler (javac) is not available
    echo   Please install JDK 17+, not just JRE
)

echo.
echo Checking Node.js...
node -v >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ Node.js is installed
    node -v
) else (
    echo ✗ Node.js is not installed or not in PATH
)

echo.
echo Checking npm...
npm -v >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ npm is installed
    npm -v
) else (
    echo ✗ npm is not installed or not in PATH
)

echo.
echo Checking Maven...
mvn -v >nul 2>&1
if %errorlevel% == 0 (
    echo ✓ Maven is installed
    mvn -v
) else (
    echo ✗ Maven is not installed or not in PATH
    echo   Note: Maven can be installed separately or comes with IDEs
)

echo.
echo Environment PATH:
echo %PATH%
echo.
echo JAVA_HOME:
echo %JAVA_HOME%
echo.
echo ========================================
echo For detailed setup instructions, see ENVIRONMENT_SETUP.md
pause