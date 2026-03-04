package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User contact information
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserContact {

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("username")
  private String username;

  @JsonProperty("firstName")
  private String firstName;

  @JsonProperty("lastName")
  private String lastName;

  @JsonProperty("email")
  private String email;

  @JsonProperty("phone")
  private String phone;

  public UserContact() {
  }

  /**
   * Constructor with userId and username only.
   * Sets firstName and lastName to empty strings to avoid SPACE validation
   * errors.
   */
  public UserContact(String userId, String username) {
    this.userId = userId;
    this.username = username;
    this.firstName = ""; // Default empty string to satisfy SPACE requirements
    this.lastName = ""; // Default empty string to satisfy SPACE requirements
  }

  /**
   * Full constructor with all contact information
   */
  public UserContact(String userId, String username, String firstName, String lastName, String email, String phone) {
    this.userId = userId;
    this.username = username;
    this.firstName = firstName != null ? firstName : "";
    this.lastName = lastName != null ? lastName : "";
    this.email = email;
    this.phone = phone;
  }

  // Getters and Setters
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String toString() {
    return "UserContact{userId='" + userId + "', username='" + username + "', firstName='" + firstName +
        "', lastName='" + lastName + "', email='" + email + "', phone='" + phone + "'}";
  }
}
