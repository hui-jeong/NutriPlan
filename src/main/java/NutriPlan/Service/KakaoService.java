package NutriPlan.Service;

import NutriPlan.model.Dto.KakaoUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import jakarta.servlet.http.HttpSession;

@Service
public class KakaoService {


    private final UserService userService;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.loginRedirect.uri}")
    private String loginRedirectUri;

    @Value("${kakao.logoutRedirect.uri}")
    private String logoutRedirectUri;

    public KakaoService( UserService userService) {
        this.userService = userService;
    }

    public String kakaoLogin(String code) throws JsonProcessingException {


        return getToken(code);
    }

    private String getToken(String code) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", loginRedirectUri);
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);
        RestTemplate restTemplate= new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        System.out.println("TOKEN: "+jsonNode.get("access_token").asText());
        return jsonNode.get("access_token").asText();
    }

    public KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        System.out.println("getKakaoUserInfo 호출됨. accessToken: " + accessToken);  // 확인

        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );
        System.out.println("카카오 API 응답: " + response.getBody());  // 응답 JSON 출력

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        System.out.println("JSON 응답 ID: " + jsonNode.get("id"));
        System.out.println("JSON 응답 닉네임: " + jsonNode.get("properties").get("nickname"));

        Long kakaoId = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();
        // profileImg = jsonNode.get("properties").get("profile_image").asText();
        System.out.println("KAKAOID/NICKNAME: "+kakaoId+nickname);

        int userId = userService.saveUser(kakaoId, nickname);
        System.out.println(userId);
        if (userId == 0) {
            System.out.println(userId+"!");
            throw new IllegalArgumentException("Kakao ID가 null입니다.");
        }

        return new KakaoUserInfoDto(kakaoId, nickname);
    }

    private String generateJwtToken(KakaoUserInfoDto kakaoUserInfo) {
        return "generatedJwtToken";
    }

    public String getKakaoAuthUrl() {
        String authUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + loginRedirectUri;
        return authUrl;
    }


    //로그아웃
    // 카카오 로그아웃 처리
//    public void kakaoLogout(String accessToken) {
//
//        validateAccessToken(accessToken);
//
//        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        try {
//            // 카카오 로그아웃 요청
//            ResponseEntity<String> logoutResponse = restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);
//            System.out.println("카카오 로그아웃 응답: " + logoutResponse.getBody());
//
////            session.invalidate();
////            System.out.println("서버 세션이 초기화되었습니다.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("카카오 로그아웃 처리 중 오류가 발생했습니다.");
//        }
//    }
//
//    private void validateAccessToken(String accessToken) {
//        String validationUrl = "https://kapi.kakao.com/v1/user/access_token_info";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<String> validationResponse = restTemplate.exchange(validationUrl, HttpMethod.GET, entity, String.class);
//            System.out.println("토큰 유효성 검사 성공: " + validationResponse.getBody());
//        } catch (Exception e) {
//            System.out.println("토큰 유효성 검사 실패: " + e.getMessage());
//            throw new RuntimeException("access_token이 유효하지 않습니다.");
//        }
//    }


//    private static final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
//
//    // 카카오 로그아웃 API 호출
//    public ResponseEntity<String> kakaoLogout(HttpEntity<String> entity) {
//        // RestTemplate을 사용하여 카카오 로그아웃 API에 POST 요청을 보냄
//        RestTemplate restTemplate = new RestTemplate();
//
//        try {
//            // 카카오 로그아웃 API에 POST 요청을 보냄
//            return restTemplate.exchange(KAKAO_LOGOUT_URL, HttpMethod.POST, entity, String.class);
//        } catch (Exception e) {
//            // 요청에 실패한 경우 예외 처리
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("로그아웃 API 호출 실패");
//        }
//    }
//    // 카카오 ID 추출 (accessToken을 이용)
//    public Long getKakaoIdFromAccessToken(String accessToken) {
//        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.POST, entity, String.class);
//
//        JsonNode jsonNode;
//        try {
//            jsonNode = new ObjectMapper().readTree(userInfoResponse.getBody());
//            return jsonNode.get("id").asLong();  // 카카오 ID 반환
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;  // 오류가 발생하면 null 반환
//        }
//    }
        public String getKakaoLogoutUrl() {
//            String clientId = "YOUR_CLIENT_ID";  // 카카오 클라이언트 ID
            String logoutRedirectUri = "http://192.168.10.103:8080/Out";  // 로그아웃 리다이렉트 URI
            return "https://kauth.kakao.com/oauth/logout?client_id=" + clientId + "&logout_redirect_uri=" + logoutRedirectUri;
        }


}
