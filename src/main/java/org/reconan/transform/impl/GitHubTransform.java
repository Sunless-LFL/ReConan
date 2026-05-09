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

public class GitHubTransform implements Transform {

    private static final String BASE = "https://api.github.com/users/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "GitHub Profile Lookup"; }
    @Override public EntityType getInputType()  { return EntityType.USERNAME; }
    @Override public EntityType getOutputType() { return EntityType.ORG; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue()))
                .header("Accept", "application/vnd.github+json");

            if (OsintConfig.GITHUB_TOKEN != null)
                reqBuilder.header("Authorization", "Bearer " + OsintConfig.GITHUB_TOKEN);

            HttpResponse<String> resp =
                http.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404) return List.of();

            JsonNode root = json.readTree(resp.body());
            List<Entity> results = new ArrayList<>();

            if (root.has("name"))        input.addMetadata("realName", root.get("name").asText());
            if (root.has("bio"))         input.addMetadata("bio",      root.get("bio").asText());
            if (root.has("location"))    input.addMetadata("location", root.get("location").asText());
            if (root.has("public_repos"))input.addMetadata("repos",    root.get("public_repos").asText());

            if (root.has("company") && !root.get("company").isNull()) {
                Entity org = new Entity(EntityType.ORG,
                    root.get("company").asText().replace("@", ""));
                org.addMetadata("source", "github");
                results.add(org);
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("GitHub lookup failed for " + input.getValue(), e);
        }
    }
}
