package io.pathops.controlplane.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pathops.controlplane.response.JSendResponse;
import io.pathops.controlplane.response.JSendResponseFactory;
import io.pathops.controlplane.service.LoginService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class LoginController {

    private final JSendResponseFactory jSendResponseFactory;
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<JSendResponse> login(@AuthenticationPrincipal Jwt jwt) {
        
    	var result = loginService.login(jwt);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jSendResponseFactory.createSuccessMessage(result));
    }
}