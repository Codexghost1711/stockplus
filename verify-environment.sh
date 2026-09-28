#!/bin/bash

echo "StockPulse Environment Verification Script"
echo "========================================"
echo

echo "Checking Java..."
if command -v java &> /dev/null; then
    echo "✓ Java is installed"
    java -version
else
    echo "✗ Java is not installed or not in PATH"
fi

echo
echo "Checking Java Compiler..."
if command -v javac &> /dev/null; then
    echo "✓ Java Compiler (javac) is available"
    javac -version
else
    echo "✗ Java Compiler (javac) is not available"
    echo "  Please install JDK, not just JRE"
fi

echo
echo "Checking Node.js..."
if command -v node &> /dev/null; then
    echo "✓ Node.js is installed"
    node -v
else
    echo "✗ Node.js is not installed or not in PATH"
fi

echo
echo "Checking npm..."
if command -v npm &> /dev/null; then
    echo "✓ npm is installed"
    npm -v
else
    echo "✗ npm is not installed or not in PATH"
fi

echo
echo "Checking Maven..."
if command -v mvn &> /dev/null; then
    echo "✓ Maven is installed"
    mvn -v
else
    echo "✗ Maven is not installed or not in PATH"
fi

echo
echo "========================================"
echo "For detailed setup instructions, see ENVIRONMENT_SETUP.md"