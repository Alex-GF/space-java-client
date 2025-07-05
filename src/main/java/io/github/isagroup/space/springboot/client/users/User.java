package io.github.isagroup.space.springboot.client.users;

public record User(String id, String username, String password, Role role, String apiKey) {

}