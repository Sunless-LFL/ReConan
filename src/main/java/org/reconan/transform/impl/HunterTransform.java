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

public class HunterTransform implements Transform {

    private static final String BASE = "https://api.hunter.io/v2/domain-search";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "Hunter.io Email Finder"; }
    @Override public EntityType getInputType()  { return EntityType.DOMAIN; }
    @Override public EntityType getOutputType() { return EntityType.EMAIL; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.HUNTER_KEY == null)
            throw new TransformException("HUNTER_API_KEY not set in .env", null);

        try {
            String url = BASE + "?domain=" + input.getValue()
                       + "&api_key=" + OsintConfig.HUNTER_KEY;

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode data = json.readTree(resp.body()).path("data");
            List<Entity> results = new ArrayList<>();

            if (!data.isMissingNode()) {
                input.addMetadata("organization", data.path("organization").asText());
                input.addMetadata("email_count",  data.path("emails").size() + "");

                for (JsonNode e : data.path("emails")) {
                    String address = e.path("value").asText();
                    if (address.isBlank()) continue;
                    Entity email = new Entity(EntityType.EMAIL, address);
                    email.addMetadata("first_name",  e.path("first_name").asText());
                    email.addMetadata("last_name",   e.path("last_name").asText());
                    email.addMetadata("position",    e.path("position").asText());
                    email.addMetadata("confidence",  e.path("confidence").asText());
                    email.addMetadata("source",      "hunter");
                    results.add(email);
                }
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("Hunter.io lookup failed for " + input.getValue(), e);
        }
    }
}
