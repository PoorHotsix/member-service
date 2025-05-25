package com.example.member_service.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class KeycloakService {

    private final Keycloak keycloak;

    public KeycloakService() {
        // 관리용 realm(master)과 client(admin-cli)로 인증
        this.keycloak = KeycloakBuilder.builder()
            .serverUrl("http://localhost:8080")
            .realm("master")
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .build();
    }

    public void createUser(String email, String username, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);

        // 비밀번호 Credential 
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        log.info("Keycloak 사용자 생성 요청: realm=inkcloud, username={}, email={}", username, email);

        Response response = keycloak.realm("inkcloud").users().create(user);

        log.info("Keycloak 응답 status: {}, location: {}", response.getStatus(), response.getLocation());

        response.close();
    }
}
