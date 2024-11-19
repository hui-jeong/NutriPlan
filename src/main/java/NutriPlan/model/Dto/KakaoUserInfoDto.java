package NutriPlan.model.Dto;

public class KakaoUserInfoDto {
    private Long kakaoId;
    private String nickname;
    //private String email;

    public KakaoUserInfoDto(Long id, String nickname) {
        this.kakaoId = id;
        this.nickname = nickname;
    }

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

