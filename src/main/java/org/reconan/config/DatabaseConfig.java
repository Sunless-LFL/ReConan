package org.reconan.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Loads database configuration from environment variables.
 */
public class DatabaseConfig {
    // Load environment variables from .env file
    private static final Dotenv dotenv = Dotenv.load();

    // Database connection parameters (DB_URL, DB_USER, and DB_PASSWORD)
    public static final String URL = dotenv.get("DB_URL");
    public static final String USER = dotenv.get("DB_USER");
    public static final String PASSWORD = dotenv.get("DB_PASSWORD");
}