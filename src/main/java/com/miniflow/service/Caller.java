package com.miniflow.service;

import java.util.List;
import java.util.Optional;

// Caller.java
public record Caller(String user, List<String> groups) {
    public static Caller fromJwt(org.springframework.security.oauth2.jwt.Jwt jwt) {
        String user = Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .orElseGet(jwt::getSubject);
        List<String> groups = Optional.ofNullable(jwt.getClaimAsStringList("groups"))
                .orElseGet(List::of);
        return new Caller(user, groups.stream().filter(s -> s!=null && !s.isBlank())
                .map(String::trim).distinct().toList());
    }
}
