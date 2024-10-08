package NutriPlan.Service;


import NutriPlan.Repository.UserRepository;
import NutriPlan.model.Dao.User;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;
    public void saveUser(Long kakaoId, String nickname){
        User user = new User(kakaoId,nickname,null,null,null,null);
        userRepository.save(user);
    }
}
