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

public class HunterEmailVerifyTransform implements Transform {

    private static final String BASE = "https://api.hunter.io/v2/email-verifier";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "Hunter.io Email Verifier"; }
    @Override public EntityType getInputType()  { return EntityType.EMAIL; }
    @Override public EntityType getOutputType() { return EntityType.PERSON; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.HUNTER_KEY == null)
            throw new TransformException("HUNTER_API_KEY not set in .env", null);

        try {
            String url = BASE + "?email=" + input.getValue() + "&api_key=" + OsintConfig.HUNTER_KEY;

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            
            JsonNode data = json.readTree(resp.body()).path("data");
            List<Entity> results = new ArrayList<>();

            if (data.isMissingNode()) return results;

            input.addProperty("status",      data.path("status").asText());
            input.addProperty("score",       data.path("score").asText());
            input.addProperty("deliverable", data.path("result").asText());
            input.addProperty("webmail",     data.path("webmail").asText());
            input.addProperty("disposable",  data.path("disposable").asText());
            input.addProperty("mx_records",  data.path("mx_records").asText());

            String firstName = data.path("first_name").asText();
            String lastName  = data.path("last_name").asText();
            if (!firstName.isBlank() || !lastName.isBlank()) {
                Entity person = new Entity(EntityType.PERSON, (firstName + " " + lastName).trim());
                person.addProperty("source",   "hunter_verify");
                person.addProperty("position", data.path("position").asText());
                person.addProperty("company",  data.path("company").asText());
                results.add(person);
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("Hunter.io email verify failed for " + input.getValue(), e);
        }
    }
}
