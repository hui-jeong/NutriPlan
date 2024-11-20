package NutriPlan.controller;

import NutriPlan.Service.KakaoService;
import NutriPlan.Service.UserService;
import NutriPlan.model.Dto.KakaoUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class KakaoLogin {
    private final KakaoService kakaoService;
    private final UserService userService;

    public KakaoLogin(KakaoService kakaoService, UserService userService) {
        this.kakaoService = kakaoService;
        this.userService = userService;
    }

    @GetMapping("/spring/login/oauth2/code/kakao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        String token = kakaoService.kakaoLogin(code);

        KakaoUserInfoDto userInfo = kakaoService.getKakaoUserInfo(token);
        Long kakaoId = userInfo.getKakaoId();
        String nick = userInfo.getNick();

        if (kakaoId == null) {
            System.out.println("Kakao ID가 null입니다.");
            return ResponseEntity.badRequest().body(Map.of("error", "Kakao ID가 null입니다."));
        }

        long userId = userService.saveUser(kakaoId, nick);


        Map<String, Object> response = new HashMap<>();
        response.put("nickname", nick);
        response.put("userId", userId);
        response.put("access_token", token);

        System.out.println("CODE: " + code);
        System.out.println("userId: " + userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/spring/kakao/login")
    public String login(){
        String kakaoAuthUrl = kakaoService.getKakaoAuthUrl();
        return "redirect:" + kakaoAuthUrl;

    }

    //로그아웃
    @GetMapping("/spring/kakao/logout")
    public String kakaoLogout(HttpServletRequest request, HttpServletResponse response, @RequestParam("access_token") String accessToken) {

        request.getSession().invalidate();


        Cookie cookie = new Cookie("access_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 카카오 로그아웃 API 호출
        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> logoutResponse = restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);

        return "redirect:/spring/kakao/login";
    }
}