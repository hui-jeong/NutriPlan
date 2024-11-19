package NutriPlan.Service;


import NutriPlan.Repository.UserRepository;
import NutriPlan.model.Dao.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    public Long saveUser(Long kakaoId, String nickname){
        User user = userRepository.findByKakaoId(kakaoId);

        if (user == null) {
            user = new User();
            user.setKakaoId(kakaoId);
            user.setNickname(nickname);
            userRepository.save(user);
            System.out.println("새 사용자 생성됨: kakaoId = " + kakaoId + ", nickname = " + nickname);
        } else {
            System.out.println("기존 사용자 존재: ID = " + user.getId());
        }
        System.out.println("찾은 사용자: " + user.getId());
        return user.getId();
    }

    public Long getUserIdByKakaoId(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId);

        if (user == null) {
            System.out.println("해당 kakaoId에 대한 사용자가 없습니다.");
            return null;
        }
        System.out.println("getUserIdByKakaoId="+user.getId());
        return user.getId();
    }

    public User updateUserInfo(Long userId, Integer age, Integer height, Integer weight, Integer gender) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다."));

        user.setAge(age);
        user.setHeight(height);
        user.setWeight(weight);
        user.setGender(gender);

        int bmr = calculateBmr(height, weight, age, gender);
        user.setBmr(bmr);

        userRepository.save(user);
        return user;
    }

    private int calculateBmr(int height, int weight, int age, int gender) {
        if (gender == 1) {  // 남성
            return (int) (88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age));
        } else {  // 여성
            return (int) (447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age));
        }
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
