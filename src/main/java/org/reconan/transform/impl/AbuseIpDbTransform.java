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

public class AbuseIpDbTransform implements Transform {

    private static final String BASE = "https://api.abuseipdb.com/api/v2/check";
    private final HttpClient   http  = HttpClient.newHttpClient();
    private final ObjectMapper json  = new ObjectMapper();

    @Override public String     getName()       { return "AbuseIPDB Reputation"; }
    @Override public EntityType getInputType()  { return EntityType.IP; }
    @Override public EntityType getOutputType() { return EntityType.ORG; }

    @Override
    public List<Entity> execute(Entity input) throws TransformException {
        if (OsintConfig.ABUSEIPDB_KEY == null)
            throw new TransformException("ABUSEIPDB_API_KEY not set in .env", null);

        try {
            String url = BASE + "?ipAddress=" + input.getValue() + "&maxAgeInDays=90&verbose";

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Key",    OsintConfig.ABUSEIPDB_KEY)
                .header("Accept", "application/json")
                .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode data = json.readTree(resp.body()).path("data");
            List<Entity> results = new ArrayList<>();

            if (!data.isMissingNode()) {
                input.addMetadata("abuse_score",    data.path("abuseConfidenceScore").asText("0"));
                input.addMetadata("total_reports",  data.path("totalReports").asText("0"));
                input.addMetadata("country",        data.path("countryCode").asText());
                input.addMetadata("usage_type",     data.path("usageType").asText());
                input.addMetadata("is_tor",         data.path("isTor").asText("false"));

                String isp = data.path("isp").asText();
                if (!isp.isBlank()) {
                    Entity org = new Entity(EntityType.ORG, isp);
                    org.addMetadata("source", "abuseipdb");
                    org.addMetadata("domain", data.path("domain").asText());
                    results.add(org);
                }
            }

            return results;

        } catch (Exception e) {
            throw new TransformException("AbuseIPDB lookup failed for " + input.getValue(), e);
        }
    }
}
