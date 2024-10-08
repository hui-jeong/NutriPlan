package NutriPlan.controller;



import NutriPlan.Service.KakaoService;
import NutriPlan.model.Dto.KakaoUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class KakaoLogin {
    private final KakaoService kakaoService;

    // 생성자를 통해 KakaoService 주입
    public KakaoLogin(KakaoService kakaoService) {
        this.kakaoService = kakaoService;
    }

    @GetMapping("/login/oauth2/code/kakao")   // kakao developers의 어플리케이션 등록 할 때 넣어줬던 path
    public String kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        String token = kakaoService.kakaoLogin(code);

        System.out.println("CODE: "+code);

        return "redirect:/";
    }


    @GetMapping("/kakao/login")
    public String login(){
        return "a/kakao";
    }
}