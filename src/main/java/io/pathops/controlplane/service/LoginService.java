package io.pathops.controlplane.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.utils.JwtUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginUserService loginUserService;
    private final LoginProjectionService loginProjectionService;

    @Transactional
    public LoginResult login(Jwt jwt) {
        String issuer = JwtUtils.getIssuer(jwt);
        String subject = JwtUtils.getSubject(jwt);
        String preferredUsername = JwtUtils.getPreferredUsername(jwt);
        String email = JwtUtils.getEmail(jwt);

        LoginResult loginResult = loginUserService.createOrUpdateUser(
            issuer,
            subject,
            preferredUsername,
            email
        );

        loginProjectionService.enqueueForLogin(
            loginResult.userId(),
            loginResult.tenantId(),
            loginResult.membershipRole()
        );

        return loginResult;
    }
}