package com.interviewagent.interviewagent_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

//utility class to handle everything related to JWT generation, validation and extraction

@Service
public class JwtService {

    @Value("${app.jwt.secret}") // inject value from application properties
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    private Key getSigningKey(){//converts secret key into java.security.Key
        //this key is used to sign or validated JWT tokens using HMAC
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //generate a JWT(JSON Web Token) for a given user
    //extra claims, ->optional information like user roles, permissions
    public String generateToken(String username, Map<String, Object> extraClaims){
        return Jwts.builder()//build jwt token
                .claims(extraClaims) // add extra info
                .subject(username) //identify the principal the jwt is for
                .issuedAt(new Date()) // the time when jwt was created
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // to add the expiry when token should expire
                .signWith(getSigningKey())
                .compact();//finish the token, <header>.<payload>.<signature>
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);// get the sub claim from jwt
    }

    public <T> T extractClaim(String token, Function<Claims, T>claimResolver){
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);// function takes Claims and returns a specific claim
        //getSubject() or getExpiration()
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()//get the parser
                .verifyWith((SecretKey) getSigningKey())//verifies the secret key
                .build()//finalizes the parser
                .parseSignedClaims(token) // parses and verifies the jwt
                .getPayload(); //returns the body of the token.
    }

    public boolean isTokenValid(String token, String username){ //check if the token's username matches the given username
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token); //make sure the token has not expred
    }

    public boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
