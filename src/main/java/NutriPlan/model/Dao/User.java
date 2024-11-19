package NutriPlan.model.Dao;




import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;

@Setter
@Getter
@Entity
public class User {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id",unique = true)
    private Long kakaoId;

    @Column(name = "nick", length = 20)
    private String nickname;

    @Column
    private Integer height;

    @Column
    private Integer weight;

    @Column
    private Integer age;

    @Column
    private Integer bmr;

    @Column
    private Integer gender;

    public User() {}

    public User(Long kakaoId,String nickname,Integer height, Integer weight, Integer age, Integer bmr,Integer gender){
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.bmr = bmr;
        this.gender = gender;
    }

    public Long getId(){
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getKakaoId() {
        return kakaoId;
    }
    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }
    public String getNickname(){
        return nickname;
    }
    public void setNickname(String nick) {
        this.nickname = nick;
    }
    public Integer getHeight(){
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight(){
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getAge(){
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public Integer getBmr(){
        return bmr;
    }
    public void setBmr(Integer bmr) {
        this.bmr = bmr;
    }

    public Integer getGender(){
        return gender;
    }
    public void setGender(Integer gender) {
        this.gender = gender;
    }

}
