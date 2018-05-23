package fetcher.security;

import fetcher.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expire}")
    private long expireInMillisec;

    public String createToken(String username, Role role, String uuid) {

        Map<String, Object> payload = new HashMap();
        payload.put("username", username);
        payload.put("role", role.name());
        payload.put("uuid", uuid);

        Claims claims = Jwts.claims(payload);


        Date now = new Date();
        Date validity = new Date(now.getTime() + expireInMillisec);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
