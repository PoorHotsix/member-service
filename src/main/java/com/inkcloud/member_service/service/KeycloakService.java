package com.inkcloud.member_service.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;


import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.RoleRepresentation;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;

import java.util.List;

@Slf4j
@Service
public class KeycloakService {

    private final Keycloak keycloak;

    public KeycloakService() {
        // 관리용 realm(master)과 client(admin-cli)로 인증
        this.keycloak = KeycloakBuilder.builder()
            .serverUrl("http://keycloak:8080")
            //.serverUrl("https://keycloak.inkcloud.click")
            .realm("master")
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .grantType(OAuth2Constants.PASSWORD)
            // 연결 풀 및 자동 토큰 갱신 설정
            .resteasyClient(
                new ResteasyClientBuilderImpl()
                    .connectionPoolSize(10)
                    .build()
            )
            .build();
    }

    
    //사용자 생성
    public void createUser(String email, String username, String password, String firstName, String lastName, String roleName) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true);

        // 비밀번호 Credential 
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));

        log.info("Keycloak 사용자 생성 요청: realm=inkcloud, username={}, email={}, rolename: {}", username, email, roleName);

        Response response = keycloak.realm("inkcloud").users().create(user);

        log.info("Keycloak 응답 status: {}, location: {}", response.getStatus(), response.getLocation());

        // 사용자 ID 추출
        String userId = null;
        if (response.getStatus() == 201 && response.getLocation() != null) {
            String location = response.getLocation().toString();
            userId = location.substring(location.lastIndexOf('/') + 1);
            log.info("아이디 추출 userid: {}, location: {}", userId, location);
        }
        response.close();

        // 역할 할당
        if (userId != null) {
            try {
                // 역할 존재 여부 확인 및 상세 로그
                var rolesResource = keycloak.realm("inkcloud").roles();
                if (rolesResource.get(roleName) == null) {
                }
                RoleRepresentation role = rolesResource.get(roleName).toRepresentation();

                // 사용자 생성 직후 약간 대기 (Keycloak 내부 인덱싱 대기)
                Thread.sleep(200);

                keycloak.realm("inkcloud").users().get(userId)
                        .roles().realmLevel().add(List.of(role));
                log.info("Keycloak 사용자에 역할 할당 완료: email={}, role={}", email, roleName);
            } catch (Exception e) {
                log.error("Keycloak 역할 할당 실패: email={}, role={}, error={}", email, roleName, e.getMessage(), e);
            }
        } else {
            log.error("Keycloak 사용자 생성 실패: userId=null, email={}", email);
        }
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


    //비밀번호 재설정 이메일 발송 
    public void sendResetPasswordEmail(String email) {
        List<UserRepresentation> users = keycloak.realm("inkcloud").users().search(email);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Keycloak에 해당 이메일의 사용자가 없습니다: " + email);
        }
        String userId = users.get(0).getId();
        // Keycloak에서 비밀번호 재설정 이메일 발송
        keycloak.realm("inkcloud").users().get(userId).executeActionsEmail(List.of("UPDATE_PASSWORD"));
        log.info("비밀번호 재설정 이메일 발송 완료: email={}", email);
    }


    //비밀번호 변경시 키클록 업데이트 
    public void updatePassword(String email, String newPassword) {
        try {
            
            // 이메일로 사용자 검색
            List<UserRepresentation> users = keycloak.realm("inkcloud").users().search(email);
            if (users.isEmpty()) {
                throw new IllegalArgumentException("Keycloak에 해당 이메일의 사용자가 없습니다");
            }
            
            String userId = users.get(0).getId();
            
            // 비밀번호 변경을 위한 Credential 객체 생성
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);
            
            log.info("비밀번호 변경 credential 객체 생성: type={}, temporary={}", 
                credential.getType(), credential.isTemporary());
        
            // 비밀번호 업데이트
            keycloak.realm("inkcloud").users().get(userId).resetPassword(credential);
            
            log.info("Keycloak 사용자 비밀번호 변경 성공: email={}", email);
        
        } catch (Exception e) {
            throw new RuntimeException("Keycloak 사용자 비밀번호 변경 실패", e);
        }
    }

    // 회원가입 실패시 이미 keycloak에 등록된 사용자 찾아서 삭제
    public void deleteUser(String email) {

        List<UserRepresentation> users = keycloak.realm("inkcloud").users().search(email);

        if (!users.isEmpty()) {
            String userId = users.get(0).getId();
            keycloak.realm("inkcloud").users().delete(userId);
            log.info("Keycloak 사용자 삭제: email={}, userId={}", email, userId);
        } else {
            log.warn("Keycloak 사용자 삭제 실패: email={} (사용자 없음)", email);
        }
    }


    //재가입시 회원정보(성, 이름, 비밀번호) 변경
    public void updateUserInfo(String email, String password, String firstName, String lastName) {
        List<UserRepresentation> users = keycloak.realm("inkcloud").users().search(email);
        if (users.isEmpty()) return;
        String userId = users.get(0).getId();

        // 비밀번호 변경
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        keycloak.realm("inkcloud").users().get(userId).resetPassword(credential);

        // 성/이름 변경
        UserRepresentation user = users.get(0);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        keycloak.realm("inkcloud").users().get(userId).update(user);
    }


}
