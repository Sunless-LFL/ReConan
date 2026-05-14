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

public class ShodanTransform implements Transform {

    private static final String BASE = "https://api.shodan.io/shodan/host/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "Shodan Host Lookup"; }
    @Override public EntityType getInputType()  { return EntityType.IP_ADDRESS; }
    @Override public EntityType getOutputType() { return EntityType.ORGANIZATION; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.SHODAN_KEY == null)
            throw new TransformException("SHODAN_API_KEY not set in .env", null);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue() + "?key=" + OsintConfig.SHODAN_KEY))
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404) return List.of();

            JsonNode root = json.readTree(resp.body());
            List<Entity> results = new ArrayList<>();

            if (root.has("country_name")) input.addProperty("country",  root.get("country_name").asText());
            if (root.has("city"))         input.addProperty("city",     root.get("city").asText());
            if (root.has("isp"))          input.addProperty("isp",      root.get("isp").asText());
            if (root.has("os") && !root.get("os").isNull())
                                          input.addProperty("os",       root.get("os").asText());

            if (root.has("ports")) {
                StringBuilder ports = new StringBuilder();
                for (JsonNode p : root.get("ports")) ports.append(p.asInt()).append(",");
                if (ports.length() > 0) ports.setLength(ports.length() - 1);
                input.addProperty("open_ports", ports.toString());
            }

            if (root.has("vulns")) {
                StringBuilder vulns = new StringBuilder();
                root.get("vulns").fieldNames().forEachRemaining(v -> vulns.append(v).append(","));
                if (vulns.length() > 0) vulns.setLength(vulns.length() - 1);
                input.addProperty("vulns", vulns.toString());
            }

            if (root.has("org")) {
                Entity org = new Entity(EntityType.ORGANIZATION, root.get("org").asText());
                org.addProperty("source", "shodan");
                results.add(org);
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("Shodan lookup failed for " + input.getValue(), e);
        }
    }
}
