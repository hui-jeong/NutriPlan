package NutriPlan.model.Dto;

public class UserResponseDto {
    private Long userId;
    private String nickname;
    private Integer bmr;

    public UserResponseDto(Long userId, String nickname, Integer bmr) {
        this.userId = userId;
        this.nickname = nickname;
        this.bmr = bmr;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getBmr() {
        return bmr;
    }
}
