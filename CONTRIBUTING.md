# Contributing to ReConan

First off, thank you for considering contributing to ReConan! It's people like you that make ReConan such a great tool.

## Getting Started

1. **Fork the repository**: Click the "Fork" button at the top right of this page to create your own copy of the repository.
2. **Clone the fork**: Clone your fork to your local machine.
   ```bash
   git clone https://github.com/YOUR-USERNAME/ReConan.git
   ```
3. **Create a branch**: Create a new branch for your feature or bug fix.
   ```bash
   git checkout -b feature/my-awesome-feature
   ```

## Development Environment Setup

### Prerequisites
Before you begin, ensure you have the following installed:
- **Java Development Kit (JDK) 17+** (or whichever version the project uses, though recent JavaFX projects typically use 17+).
- **Maven 3.6+** for dependency management and building.
- A running instance of **SQL Server**.
- **SQL Server Configuration**:
  1. **Enable TCP/IP**: Open *SQL Server Configuration Manager*, go to *SQL Server Network Configuration* > *Protocols for MSSQLSERVER*, and set **TCP/IP** to **Enabled**.
  2. **Configure Port**: In *TCP/IP Properties* > *IP Addresses* tab, scroll to **IP All** and set **TCP Port** to `1433`.
  3. **Restart Service**: Restart the *SQL Server (MSSQLSERVER)* service.
  4. **Create Database**: Open *SQL Server Management Studio (SSMS)* or any SQL tool and run:
     ```sql
     CREATE DATABASE ReConan;
     ```

### Environment Variables
You will need to set up your environment variables for the database connection and other configurations.
We provide an example configuration file named `.env.example`. You should copy this file and rename it to `.env` in the root directory:

#### On Linux/macOS
```bash
cp .env.example .env
```

#### On Windows (PowerShell)
```powershell
Copy-Item .env.example .env
```

Open the newly created `.env` file and replace the placeholder values with your actual database credentials. Ensure your `.env` is **never** committed to version control (it should be included in `.gitignore`).

### Compilation and Running
ReConan is built with Maven and JavaFX. You can compile and run the application locally with the following command:
```bash
mvn javafx:run
```

## Making Changes
- Ensure your code follows the existing style and modular architecture.
- Test your changes thoroughly before submitting.

## Pull Requests
1. Push your changes to your fork:
   ```bash
   git push origin feature/my-awesome-feature
   ```
2. Open a **Pull Request (PR)** from your fork to the main ReConan repository.
3. Provide a clear and detailed description of the changes you have made.

We will review your PR as soon as possible. Thank you for your contribution!