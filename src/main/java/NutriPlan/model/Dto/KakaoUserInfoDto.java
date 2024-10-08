package NutriPlan.model.Dto;

public class KakaoUserInfoDto {
    private Long kakaoId; // 카카오 사용자 ID
    private String nickname; // 사용자의 닉네임
    //private String email; // 사용자의 이메일

    // 생성자
    public KakaoUserInfoDto(Long id, String nickname) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
    }

    // Getter와 Setter
    public Long getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(Long id) {
        this.kakaoId  = kakaoId ;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }



}

