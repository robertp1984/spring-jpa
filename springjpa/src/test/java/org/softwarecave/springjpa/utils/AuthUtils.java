package org.softwarecave.springjpa.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

public class AuthUtils {

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtAuth(String... authorityNames) {
        List<GrantedAuthority> authorities = Arrays.stream(authorityNames)
                .map(authorityName -> new SimpleGrantedAuthority(authorityName))
                .collect(Collectors.toList());

        return jwt().authorities(authorities);
    }
}
