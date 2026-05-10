package org.reconan.config;

import io.github.cdimascio.dotenv.Dotenv;

public class OsintConfig {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        return dotenv.get(key, null);
    }

    public static final String IPINFO_KEY          = dotenv.get("IPINFO_API_KEY",          null);
    public static final String VIRUSTOTAL_KEY      = dotenv.get("VIRUSTOTAL_API_KEY",      null);
    public static final String SHODAN_KEY          = dotenv.get("SHODAN_API_KEY",          null);
    public static final String URLSCAN_KEY         = dotenv.get("URLSCAN_API_KEY",         null);
    public static final String HIBP_KEY            = dotenv.get("HIBP_API_KEY",            null);
    public static final String GITHUB_TOKEN        = dotenv.get("GITHUB_TOKEN",            null);
    public static final String ABUSEIPDB_KEY       = dotenv.get("ABUSEIPDB_API_KEY",       null);
    public static final String HUNTER_KEY          = dotenv.get("HUNTER_API_KEY",          null);
    public static final String SECURITYTRAILS_KEY  = dotenv.get("SECURITYTRAILS_API_KEY",  null);
    public static final String CENSYS_ID           = dotenv.get("CENSYS_API_ID",           null);
    public static final String CENSYS_SECRET       = dotenv.get("CENSYS_API_SECRET",       null);
}
