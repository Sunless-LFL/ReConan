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

public class HunterEmailFinderTransform implements Transform {

    private static final String BASE = "https://api.hunter.io/v2/email-finder";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "Hunter.io Email Finder"; }
    @Override public EntityType getInputType()  { return EntityType.PERSON; }
    @Override public EntityType getOutputType() { return EntityType.EMAIL; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.HUNTER_KEY == null)
            throw new TransformException("HUNTER_API_KEY not set in .env", null);

        String[] parts = input.getValue().trim().split("\\s+");
        if (parts.length < 2)
            throw new TransformException(
                "Person value must be 'First Last domain.com' — e.g. 'Dario Amodei anthropic.com'", null);

        String domain = null;
        int domainIndex = -1;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].contains(".")) { domain = parts[i]; domainIndex = i; break; }
        }
        if (domain == null)
            throw new TransformException(
                "No domain found in value. Format: 'First Last domain.com'", null);

        String firstName = parts[0];
        String lastName  = domainIndex >= 2 ? parts[domainIndex - 1] : (domainIndex == 1 ? "" : parts[1]);

        try {
            String url = BASE
                + "?domain="     + domain
                + "&first_name=" + firstName
                + "&last_name="  + lastName
                + "&api_key="    + OsintConfig.HUNTER_KEY;

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            
            JsonNode data = json.readTree(resp.body()).path("data");
            List<Entity> results = new ArrayList<>();

            if (data.isMissingNode()) return results;

            String email = data.path("email").asText();
            if (email.isBlank()) return results;

            Entity emailEntity = new Entity(EntityType.EMAIL, email);
            emailEntity.addProperty("score",        data.path("score").asText());
            emailEntity.addProperty("position",     data.path("position").asText());
            emailEntity.addProperty("company",      data.path("company").asText());
            emailEntity.addProperty("linkedin_url", data.path("linkedin_url").asText());
            emailEntity.addProperty("source",       "hunter_finder");

            input.addProperty("position",     data.path("position").asText());
            input.addProperty("company",      data.path("company").asText());
            input.addProperty("linkedin_url", data.path("linkedin_url").asText());

            results.add(emailEntity);
            return results;

        } catch (Exception e) {
            throw new TransformException("Hunter.io email finder failed for " + input.getValue(), e);
        }
    }
}
