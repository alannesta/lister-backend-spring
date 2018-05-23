package fetcher.controllers;

import fetcher.models.User;
import fetcher.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public String login(@RequestBody Map<String, String> payload) {
        return authService.login(payload.get("username"), payload.get("password"));
    }

    @GetMapping("/testuser")
    public User testUser(@RequestParam(name="username") String username) {
        return authService.testUser(username);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Incorrect Credentials")
    public String AuthenticationExceptionHandler() {
        return "Unauthourized";
    }
}
