package sili;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Data transfer object for incoming APACHE II submissions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApachePayload {
    @JsonProperty("clientId")   public String clientId;
    @JsonProperty("authToken")  public String authToken;
    @JsonProperty("patientName")public String patientName;
    @JsonProperty("uhid")       public String uhid;
    @JsonProperty("timestamp")  public Instant timestamp;
    @JsonProperty("submissionId")public String submissionId;
    @JsonProperty("apacheInputs")public Map<String,Object> apacheInputs;
}
