package org.reconan.config;

import io.github.cdimascio.dotenv.Dotenv;

public class OsintConfig {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        return dotenv.get(key, null);
    }

    public static final String SHODAN_KEY = dotenv.get("SHODAN_API_KEY", null);
    public static final String HUNTER_KEY = dotenv.get("HUNTER_API_KEY", null);
}