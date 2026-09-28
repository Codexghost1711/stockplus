# StockPlus Environment Setup Guide

## Prerequisites

To run the StockPlus application locally, you need to have the following installed:

### Java Development Kit (JDK)
- **Required Version**: Java 17 or higher
- **Download**: [Adoptium OpenJDK](https://adoptium.net/) - Select JDK 17 or newer
- **Verify Installation**: 
  ```bash
  java -version
  javac -version
  ```

### Node.js and npm
- **Required Version**: Node.js 16+ (preferably 18+)
- **Download**: [Node.js Official Site](https://nodejs.org/)
- **Verify Installation**:
  ```bash
  node -v
  npm -v
  ```

### Apache Maven
- **Required Version**: Maven 3.6+
- **Note**: Usually bundled with IDEs or can be downloaded separately
- **Verify Installation**:
  ```bash
  mvn -v
  ```

## Installation Steps

### 1. Install Java JDK 17+

#### Windows
1. Download the latest JDK 17+ from [Adoptium](https://adoptium.net/)
2. Run the installer and follow the instructions
3. Add JAVA_HOME environment variable:
   - Open System Properties → Advanced → Environment Variables
   - Add new system variable: `JAVA_HOME` pointing to your JDK installation directory
   - Add `%JAVA_HOME%\bin` to your PATH variable

#### macOS/Linux
Using SDKMAN (recommended):
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install JDK 17+
sdk list java
sdk install java 17.0.8-tem
```

### 2. Install Node.js

#### Windows/macOS
1. Download from [Node.js official website](https://nodejs.org/)
2. Choose LTS version (recommended)
3. Run the installer

#### Linux (Ubuntu/Debian)
```bash
# Using NodeSource repository
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs
```

### 3. Verify All Installations
After installation, verify everything works:
```bash
java -version
javac -version
node -v
npm -v
mvn -v
```

## Running the Applications

### Backend (Spring Boot)
Navigate to the backend directory:
```bash
cd backend
```

Run tests:
```bash
mvn test
```

Run the application:
```bash
mvn spring-boot:run
```

The backend will be available at: http://localhost:8080

### Frontend (React/Vite)
In a new terminal, navigate to the frontend directory:
```bash
cd frontend
```

Install dependencies:
```bash
npm install
```

Start development server:
```bash
npm run dev
```

The frontend will be available at: http://localhost:5173

## Building for Production

### Backend
```bash
cd backend
mvn clean package
```

This creates a standalone JAR file in `target/` directory.

### Frontend
```bash
cd frontend
npm run build
```

This creates optimized production files in `dist/` directory.

## Troubleshooting

### Common Java Issues

1. **"No compiler is provided" Error**
   - Ensure you installed JDK, not JRE
   - Check JAVA_HOME points to JDK, not JRE
   - Verify javac is available in PATH

2. **Maven Not Found**
   - Download Maven from https://maven.apache.org/
   - Extract to a directory and add to PATH
   - Or use IDE bundled Maven

### Common Node Issues

1. **npm Permission Errors**
   ```bash
   # Configure npm to use a different directory
   mkdir ~/.npm-global
   npm config set prefix '~/.npm-global'
   # Add to PATH in ~/.bashrc or ~/.zshrc
   export PATH=~/.npm-global/bin:$PATH
   ```

2. **PowerShell Execution Policy Issues (Windows)**
   ```powershell
   # Run as Administrator
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

## Development Workflow

1. Start backend server first: `cd backend && mvn spring-boot:run`
2. In another terminal, start frontend: `cd frontend && npm run dev`
3. Access application at http://localhost:5173
4. Changes to frontend code will hot-reload automatically
5. Backend changes require restart or enable dev-tools for auto-restart