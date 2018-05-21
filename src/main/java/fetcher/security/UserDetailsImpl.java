package fetcher.security;

import fetcher.models.User;
import fetcher.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(decodePassword(user.getPassword(), user.getSalt()))
                .authorities(user.getRole().name())
                .build();
    }

    private String decodePassword(String password, String salt) {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(salt, 100, 512);
        return "placehodler";
    }

}