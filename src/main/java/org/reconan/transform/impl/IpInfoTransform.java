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

public class IpInfoTransform implements Transform {

    private final HttpClient http   = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @Override public String     getName()       { return "IP Info (Geo / ASN)"; }
    @Override public EntityType getInputType()  { return EntityType.IP; }
    @Override public EntityType getOutputType() { return EntityType.ORG; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        String token = OsintConfig.IPINFO_KEY != null
            ? "?token=" + OsintConfig.IPINFO_KEY : "";
        String url = "https://ipinfo.io/" + input.getValue() + "/json" + token;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp =
                http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = json.readTree(resp.body());
            List<Entity> results = new ArrayList<>();

            if (root.has("country")) input.addMetadata("country", root.get("country").asText());
            if (root.has("city"))    input.addMetadata("city",    root.get("city").asText());
            if (root.has("region"))  input.addMetadata("region",  root.get("region").asText());

            if (root.has("org")) {
                Entity org = new Entity(EntityType.ORG, root.get("org").asText());
                org.addMetadata("source", "ipinfo");
                results.add(org);
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("IPInfo lookup failed for " + input.getValue(), e);
        }
    }
}
