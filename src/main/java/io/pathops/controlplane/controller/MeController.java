package io.pathops.controlplane.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.pathops.controlplane.response.JSendResponse;
import io.pathops.controlplane.response.JSendResponseFactory;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class MeController {

    private final JSendResponseFactory jSendResponseFactory;
//    private final MeService meService;
	
    // TODO: Needs Implementation
    @GetMapping("/me")
    public ResponseEntity<JSendResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jSendResponseFactory.createSuccessMessage("FIXME"));
    }
}