package fetcher.controllers;

import fetcher.models.User;
import fetcher.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/login")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("")
    public String login(@RequestBody Map<String, String> payload) throws AuthenticationException {
//        try {
//            authService.login(payload.get("username"), payload.get("password"));
//            return "OK";
//        } catch(Exception e) {
//            log.error("login failed: ", e);
//            return "Failed";
//        }
        authService.login(payload.get("username"), payload.get("password"));
        return "OK";

    }

    @GetMapping("/testuser")
    public User testUser(@RequestParam(name="username") String username) {
        return authService.testUser(username);
    }
}
