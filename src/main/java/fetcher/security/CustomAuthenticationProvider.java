package fetcher.security;

import fetcher.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String providedPassword = authentication.getCredentials().toString();

        String salt = userRepository.findByUsername(username).getSalt();
        String expectedPassword = userRepository.findByUsername(username).getPassword();

        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(salt, 100, 512);
        log.info("expected: {}, actual: {}", expectedPassword, encoder.encode(providedPassword));

        if (encoder.encode(providedPassword).equals(expectedPassword)) {
            return new UsernamePasswordAuthenticationToken(username, expectedPassword);
        } else {
            log.info("not matching, return null");
            throw new BadCredentialsException("User authentication failed");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
