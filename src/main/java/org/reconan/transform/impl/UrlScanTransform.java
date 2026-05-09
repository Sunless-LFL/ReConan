package org.reconan.transform.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reconan.config.OsintConfig;
import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import org.reconan.transform.Transform;
import org.reconan.transform.TransformException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UrlScanTransform implements Transform {

    private static final String BASE = "https://urlscan.io/api/v1/search/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "URLScan Page Analysis"; }
    @Override public EntityType getInputType()  { return EntityType.URL; }
    @Override public EntityType getOutputType() { return EntityType.DOMAIN; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        try {
            String query = URLEncoder.encode("page.url:\"" + input.getValue() + "\"",
                StandardCharsets.UTF_8);
            String url = BASE + "?q=" + query + "&size=5";

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json");

            if (OsintConfig.URLSCAN_KEY != null)
                builder.header("API-Key", OsintConfig.URLSCAN_KEY);

            HttpResponse<String> resp = http.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

            JsonNode results = json.readTree(resp.body()).path("results");
            List<Entity> entities = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();

            for (JsonNode scan : results) {
                JsonNode page  = scan.path("page");
                JsonNode stats = scan.path("stats");

                String domain = page.path("domain").asText();
                if (!domain.isBlank() && seen.add(domain)) {
                    Entity d = new Entity(EntityType.DOMAIN, domain);
                    d.addMetadata("source",      "urlscan");
                    d.addMetadata("ip",          page.path("ip").asText());
                    d.addMetadata("server",      page.path("server").asText());
                    d.addMetadata("status_code", page.path("status").asText());
                    d.addMetadata("malicious",   stats.path("malicious").asText("0"));
                    entities.add(d);
                }

                String verdict = scan.path("verdicts").path("overall").path("malicious").asText("false");
                input.addMetadata("urlscan_malicious", verdict);
            }

            return entities;

        } catch (Exception e) {
            throw new TransformException("URLScan lookup failed for " + input.getValue(), e);
        }
    }
}
