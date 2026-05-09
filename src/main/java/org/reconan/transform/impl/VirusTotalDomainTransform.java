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

public class VirusTotalDomainTransform implements Transform {

    private static final String BASE = "https://www.virustotal.com/api/v3/domains/";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "VirusTotal Domain Report"; }
    @Override public EntityType getInputType()  { return EntityType.DOMAIN; }
    @Override public EntityType getOutputType() { return EntityType.IP; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.VIRUSTOTAL_KEY == null)
            throw new TransformException("VIRUSTOTAL_API_KEY not set in .env", null);

        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + input.getValue()))
                .header("x-apikey", OsintConfig.VIRUSTOTAL_KEY)
                .GET().build();

            HttpResponse<String> resp =
                http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode attrs = json.readTree(resp.body())
                .path("data").path("attributes");

            List<Entity> results = new ArrayList<>();

            JsonNode stats = attrs.path("last_analysis_stats");
            if (!stats.isMissingNode()) {
                input.addMetadata("vt_malicious",  stats.path("malicious").asText("0"));
                input.addMetadata("vt_suspicious", stats.path("suspicious").asText("0"));
            }

            JsonNode resolutions = attrs.path("last_dns_records");
            for (JsonNode r : resolutions) {
                if ("A".equals(r.path("type").asText())) {
                    Entity ip = new Entity(EntityType.IP, r.path("value").asText());
                    ip.addMetadata("source", "virustotal_dns");
                    results.add(ip);
                }
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("VirusTotal lookup failed for " + input.getValue(), e);
        }
    }
}
