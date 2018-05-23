package fetcher.security.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends GenericFilterBean {
    @Value("${security.jwt.secret}")
    private String secretKey;

    private static final String AUTH_HEADER = "auth";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String header = req.getHeader(AUTH_HEADER);

        if (header == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(AUTH_HEADER);
        if (token != null) {
            try {
                Claims claim = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody();
                if (claim.get("username") != null) {
                    List<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>();
                    roles.add(new SimpleGrantedAuthority(claim.get("role").toString()));
                    return new UsernamePasswordAuthenticationToken(claim.get("username"), null, roles);
                }
                return null;
            } catch (JwtException e) {
                log.error("JWT validation faield: {}", e);
                throw e;
            }

        }
        return null;
    }
}
