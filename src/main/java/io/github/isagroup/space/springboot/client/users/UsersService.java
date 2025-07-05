package io.github.isagroup.space.springboot.client.users;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "/users", accept = MediaType.APPLICATION_JSON_VALUE)
public interface UsersService {

    @GetExchange
    List<User> getUsers();

    @PostExchange
    User createUser(@RequestBody NewUserRequest user);

    @GetExchange("/{username}")
    User getUserByUsername(@PathVariable String username);

    @PutExchange("/{username}")
    User updateUserByUsername(@PathVariable String username, @RequestBody User user);

    @DeleteExchange("/{username}")
    void deleteUserByUsername(@PathVariable String username);

    @PutExchange("/{username}/api-key")
    User updateUserApiKey(@PathVariable String username);

    @PutExchange("/{username}/role")
    User updateUserRole(@PathVariable String username, @RequestBody RoleRequest role);
}
