package com.inkcloud.member_service.service;

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

    public void createUser(String email, String username, String password, String firstName, String lastName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true); // ← 이메일 인증 상태로 저장

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


    //회원탈퇴시 keycloak 에서 비활성화 처리 
    public void disableUser(String email) {
        UserRepresentation user = keycloak.realm("inkcloud").users().search(email).get(0);
        user.setEnabled(false);
        keycloak.realm("inkcloud").users().get(user.getId()).update(user);
    }

    // 재가입시 keycloak 회원 활성화
    public void enableUser(String email) {
        List<UserRepresentation> users = keycloak.realm("inkcloud").users().search(email);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Keycloak에 해당 이메일의 사용자가 없습니다: " + email);
        }
        UserRepresentation user = users.get(0);
        user.setEnabled(true);
        keycloak.realm("inkcloud").users().get(user.getId()).update(user);
        log.info("Keycloak 사용자 활성화 완료: email={}", email);
    }
}
