package com.maxkruiswegt.api.security;

import com.maxkruiswegt.api.models.account.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class JwtProvider {

    private final JwtKeyProvider jwtKeyProvider;
    private final MyUserDetailsService userDetailsService;

    public JwtProvider(JwtKeyProvider jwtKeyProvider, MyUserDetailsService userDetailsService) {
        this.jwtKeyProvider = jwtKeyProvider;
        this.userDetailsService = userDetailsService;
    }

    public String createToken(User user) {
        Calendar expiration = Calendar.getInstance();
        expiration.add(Calendar.HOUR, 1);

        return Jwts.builder()
                .issuer("bank.maxkruiswegt.com")
                .subject(user.getEmail())
                .expiration(expiration.getTime())
                .issuedAt(Calendar.getInstance().getTime())
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .signWith(jwtKeyProvider.getPrivateKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtKeyProvider.getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
