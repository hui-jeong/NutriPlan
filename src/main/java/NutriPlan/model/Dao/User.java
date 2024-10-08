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

    public User(){}

    public User(Long kakaoId,String nickname,Integer height, Integer weight, Integer age, Integer bmr){
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.bmr = bmr;
    }



}
