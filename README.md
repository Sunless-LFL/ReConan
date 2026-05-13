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
- **Graph Visualization**: JavaFX SmartGraph
- **Database**: SQL Server
- **Data Access**: JDBC
- **Build Tool**: Maven

## Architecture
ReConan follows a clean, **layered architecture** designed for modularity and maintainability:

```mermaid
graph TD
    UI[User Interface - JavaFX / FXML] --> Controller[Controllers]
    Controller --> Graph[Graph Manager - SmartGraph]
    Controller --> Repo[Repositories]
    Graph --> Model[Entity Models]
    Repo --> Model
    Repo --> DB[(SQL Server)]
```

### 🏛️ Layered Structure
1. **User Interface (JavaFX)**:
   - Uses **FXML** for layout and **CSS** for professional styling.
   - **ViewLoader** manages scene transitions and FXML loading.
2. **Controller Layer**:
   - Manages UI events and coordinates between the Graph Manager and Data Repositories.
   - Implements complex session-to-database persistence logic (ID mapping).
3. **Graph Management**:
   - Built on **JavaFX SmartGraph**, providing a dynamic, force-directed graph environment.
   - **GraphManager** encapsulates graph logic, custom styling, and layout physics.
4. **Data Access Layer (Repositories)**:
   - Uses the **Repository Pattern** with JDBC for database interactions.
   - Handles CRUD operations for investigations, entities, and relationships.
5. **Entity Models**:
   - Plain Java Objects (POJOs) representing the core domain: `Investigation`, `Entity`, and `Relationship`.
6. **Persistence Layer**:
   - **SQL Server** database with a relational schema optimized for entity-relationship mapping.
   - **DatabaseManager** handles automated schema initialization and connectivity.

## Contributing
We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on how to fork the repository, set up your `.env` file, compile the project using `mvn javafx:run`, and submit Pull Requests.

## Contributors
Thanks to these wonderful people for contributing to ReConan!

<a href="https://github.com/SBAI-Youness/ReConan/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=SBAI-Youness/ReConan" />
</a>