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
import java.util.Base64;
import java.util.List;

public class CensysTransform implements Transform {

    private static final String BASE = "https://search.censys.io/api/v2/hosts/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "Censys Host Intelligence"; }
    @Override public EntityType getInputType()  { return EntityType.IP; }
    @Override public EntityType getOutputType() { return EntityType.ORG; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.CENSYS_ID == null || OsintConfig.CENSYS_SECRET == null)
            throw new TransformException("CENSYS_API_ID or CENSYS_API_SECRET not set in .env", null);

        try {
            String credentials = OsintConfig.CENSYS_ID + ":" + OsintConfig.CENSYS_SECRET;
            String basicAuth   = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue()))
                .header("Authorization", "Basic " + basicAuth)
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404) return List.of();

            JsonNode result = json.readTree(resp.body()).path("result");
            List<Entity> results = new ArrayList<>();

            JsonNode location = result.path("location");
            if (!location.isMissingNode()) {
                input.addMetadata("country", location.path("country").asText());
                input.addMetadata("city",    location.path("city").asText());
            }

            JsonNode asn = result.path("autonomous_system");
            if (!asn.isMissingNode()) {
                input.addMetadata("asn",      asn.path("asn").asText());
                input.addMetadata("bgp_prefix", asn.path("bgp_prefix").asText());

                String orgName = asn.path("name").asText();
                if (!orgName.isBlank()) {
                    Entity org = new Entity(EntityType.ORG, orgName);
                    org.addMetadata("source",      "censys");
                    org.addMetadata("asn",         asn.path("asn").asText());
                    org.addMetadata("bgp_prefix",  asn.path("bgp_prefix").asText());
                    results.add(org);
                }
            }

            JsonNode services = result.path("services");
            if (!services.isMissingNode() && services.isArray()) {
                StringBuilder ports = new StringBuilder();
                for (JsonNode svc : services)
                    ports.append(svc.path("port").asInt()).append("/")
                         .append(svc.path("service_name").asText("?")).append(" ");
                input.addMetadata("services", ports.toString().trim());
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("Censys lookup failed for " + input.getValue(), e);
        }
    }
}
