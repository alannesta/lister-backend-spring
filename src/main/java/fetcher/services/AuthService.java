package fetcher.services;

import fetcher.models.User;
import fetcher.repositories.UserRepository;
import fetcher.security.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    CustomAuthenticationProvider customAuthenticationProvider;
    @Autowired
    UserRepository userRepository;

    public void login(String username, String password) throws AuthenticationException {
        customAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    public User testUser(String username) {
        return userRepository.findByUsername(username);
    }
}
