<p align="center">
  <img src="src/main/resources/images/icon.png" alt="ReConan Logo" width="200"/>
</p>

<h1 align="center">ReConan</h1>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License"></a>
  <a href="https://github.com/SBAI-Youness/ReConan/releases"><img src="https://img.shields.io/badge/version-v1.0-blue" alt="Version"></a>
  <a href="https://en.wikipedia.org/wiki/Java_(programming_language)"><img src="https://img.shields.io/badge/language-Java-blue" alt="Language"></a>
  <a href="https://github.com/SBAI-Youness/ReConan/pulls"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="Contributions"></a>
</p>

ReConan is a **Desktop-based OSINT and cyber investigation platform**. It aggregates intelligence from multiple public data sources, storing and visualizing relationships between digital entities (such as domains, IPs, emails, and usernames).

> **Fun Fact:** The name *ReConan* is just a highly sophisticated portmanteau of **Reconnaissance** and **Conan the Detective**. Because who better to investigate cyber threats than a brilliant, permanently-young anime detective?

## Use Case
ReConan is designed for **digital reconnaissance and relationship analysis**. Investigators can use the platform to seamlessly connect the dots between various online identities and infrastructure elements. It serves as an interactive environment to discover, store, and explore entity relationships through a dynamic investigation graph.

## Technologies Used
- **Language**: Java
- **User Interface**: JavaFX
- **Database**: SQL Server
- **Data Access**: JDBC
- **JSON Parsing**: Jackson / Gson
- **Graph Modeling**: JGraphT
- **Networking**: Java HTTP Client
- **Packaging**: jpackage

## Architecture
ReConan follows a modular, **layered architecture**:
1. **User Interface (JavaFX)**: The interactive graphical environment for investigations.
2. **Application Layer**: Contains the Investigation Services, Transform Engine, and Graph Manager.
3. **Data Layer**: Houses Repositories, DAOs, and Entity Models.
4. **Database**: SQL Server storage for targets, entities, relationships, and investigation logs.

## Contributing
We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on how to fork the repository, set up your `.env` file, compile the project using `mvn javafx:run`, and submit Pull Requests.
