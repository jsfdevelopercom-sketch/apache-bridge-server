package sili;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reply from the server/desktop back to mobile clients.
 */
public class ServerAck {
    public enum Status { SUCCESS, ERROR }
    @JsonProperty("clientId") public String clientId;
    @JsonProperty("status")   public Status status;
    @JsonProperty("message")  public String message;
    @JsonProperty("uhid")     public String uhid;
    @JsonProperty("patientName")public String patientName;

    public ServerAck(){}
    public ServerAck(String clientId, Status status, String message,
                     String uhid, String patientName){
        this.clientId = clientId;
        this.status = status;
        this.message = message;
        this.uhid = uhid;
        this.patientName = patientName;
    }
}
