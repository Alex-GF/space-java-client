package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;

/**
 * Billing period information for a contract
 */
public class BillingPeriod {

  @JsonProperty("startDate")
  private Date startDate;

  @JsonProperty("endDate")
  private Date endDate;

  @JsonProperty("autoRenew")
  private boolean autoRenew;

  @JsonProperty("renewalDays")
  private int renewalDays;

  public BillingPeriod() {
  }

  public BillingPeriod(Date startDate, Date endDate, boolean autoRenew, int renewalDays) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.autoRenew = autoRenew;
    this.renewalDays = renewalDays;
  }

  // Getters and Setters
  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public boolean isAutoRenew() {
    return autoRenew;
  }

  public void setAutoRenew(boolean autoRenew) {
    this.autoRenew = autoRenew;
  }

  public int getRenewalDays() {
    return renewalDays;
  }

  public void setRenewalDays(int renewalDays) {
    this.renewalDays = renewalDays;
  }

  public String toString() {
    return "BillingPeriod{startDate=" + startDate + ", endDate=" + endDate +
        ", autoRenew=" + autoRenew + ", renewalDays=" + renewalDays + "}";
  }
}
