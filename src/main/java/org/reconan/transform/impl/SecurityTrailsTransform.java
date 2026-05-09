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

public class SecurityTrailsTransform implements Transform {

    private static final String BASE = "https://api.securitytrails.com/v1/domain/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "SecurityTrails DNS History"; }
    @Override public EntityType getInputType()  { return EntityType.DOMAIN; }
    @Override public EntityType getOutputType() { return EntityType.IP; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.SECURITYTRAILS_KEY == null)
            throw new TransformException("SECURITYTRAILS_API_KEY not set in .env", null);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue()))
                .header("APIKEY", OsintConfig.SECURITYTRAILS_KEY)
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode root = json.readTree(resp.body());
            List<Entity> results = new ArrayList<>();

            JsonNode aRecords = root.path("current_dns").path("a").path("values");
            for (JsonNode record : aRecords) {
                String ip = record.path("ip").asText();
                if (!ip.isBlank()) {
                    Entity ipEntity = new Entity(EntityType.IP, ip);
                    ipEntity.addMetadata("source",       "securitytrails");
                    ipEntity.addMetadata("record_type",  "A");
                    results.add(ipEntity);
                }
            }

            JsonNode mxRecords = root.path("current_dns").path("mx").path("values");
            for (JsonNode record : mxRecords) {
                String host = record.path("hostname").asText();
                if (!host.isBlank())
                    input.addMetadata("mx_" + record.path("priority").asText(), host);
            }

            JsonNode nsRecords = root.path("current_dns").path("ns").path("values");
            for (JsonNode record : nsRecords) {
                String host = record.path("nameserver").asText();
                if (!host.isBlank())
                    input.addMetadata("ns", input.getMetadata().getOrDefault("ns", "") + host + " ");
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("SecurityTrails lookup failed for " + input.getValue(), e);
        }
    }
}
