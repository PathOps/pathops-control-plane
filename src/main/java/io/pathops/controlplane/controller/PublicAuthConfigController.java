package io.pathops.controlplane.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pathops.controlplane.dto.PublicAuthConfigResponse;

@RestController
public class PublicAuthConfigController {

    @Value("${pathops.public.base-url}")
    private String controlPlaneBaseUrl;

    @Value("${pathops.auth.issuer}")
    private String issuer;

    @Value("${pathops.auth.client-id}")
    private String clientId;

    @GetMapping("/public/auth/config")
    public ResponseEntity<PublicAuthConfigResponse> getPublicAuthConfig() {
        return ResponseEntity.ok(
            PublicAuthConfigResponse.builder()
                .controlPlaneBaseUrl(controlPlaneBaseUrl)
                .issuer(issuer)
                .clientId(clientId)
                .scopes(List.of("openid", "profile", "email"))
                .loginPath("/api/public/login")
                .build()
        );
    }
}