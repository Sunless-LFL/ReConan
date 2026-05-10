package org.reconan.transform.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reconan.config.OsintConfig;
import org.reconan.model.Entity;
import org.reconan.model.EntityType;
import org.reconan.transform.Transform;
import org.reconan.transform.TransformException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class HaveIBeenPwnedTransform implements Transform {

    private static final String BASE = "https://haveibeenpwned.com/api/v3/breachedaccount/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "HaveIBeenPwned Breaches"; }
    @Override public EntityType getInputType()  { return EntityType.EMAIL; }
    @Override public EntityType getOutputType() { return EntityType.BREACH; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.HIBP_KEY == null)
            throw new TransformException("HIBP_API_KEY not set in .env", null);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue() + "?truncateResponse=false"))
                .header("hibp-api-key", OsintConfig.HIBP_KEY)
                .header("User-Agent",   "ReConan-OSINT-Platform")
                .GET().build();

            HttpResponse<String> resp =
                http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404) return List.of();

            List<Entity> results = new ArrayList<>();
            JsonNode breaches = json.readTree(resp.body());

            for (JsonNode b : breaches) {
                String name = b.path("Name").asText("unknown");
                Entity breach = new Entity(EntityType.BREACH, name);
                breach.addMetadata("date",       b.path("BreachDate").asText());
                breach.addMetadata("pwnedCount", b.path("PwnCount").asText());
                breach.addMetadata("dataClasses", b.path("DataClasses").toString());
                breach.addMetadata("source", "hibp");
                results.add(breach);
            }
            return results;

        } catch (Exception e) {
            throw new TransformException("HIBP lookup failed for " + input.getValue(), e);
        }
    }
}
