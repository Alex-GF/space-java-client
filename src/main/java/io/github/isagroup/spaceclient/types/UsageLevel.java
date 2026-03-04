package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * Usage level information for a feature
 */
public class UsageLevel {
    
    @JsonProperty("resetTimeStamp")
    private Date resetTimeStamp;
    
    @JsonProperty("consumed")
    private double consumed;

    public UsageLevel() {
    }

    public UsageLevel(Date resetTimeStamp, double consumed) {
        this.resetTimeStamp = resetTimeStamp;
        this.consumed = consumed;
    }

    // Getters and Setters
    public Date getResetTimeStamp() {
        return resetTimeStamp;
    }

    public void setResetTimeStamp(Date resetTimeStamp) {
        this.resetTimeStamp = resetTimeStamp;
    }

    public double getConsumed() {
        return consumed;
    }

    public void setConsumed(double consumed) {
        this.consumed = consumed;
    }

    public String toString() {
        return "UsageLevel{resetTimeStamp=" + resetTimeStamp + ", consumed=" + consumed + "}";
    }
}
