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

@Service
public class KakaoService {

    @Autowired
    private final UserService userService;

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.redirect.uri}")
    private String redirectUrl;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;
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
        body.add("redirect_uri", redirectUrl);
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
        String authUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;
        return authUrl;
    }


    //로그아웃
    public void kakaoLogout(String accessToken) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v1/user/logout")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<Void> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .build();

        RestTemplate restTemplate = new RestTemplate();


        restTemplate.exchange(requestEntity, Void.class);
    }

}
