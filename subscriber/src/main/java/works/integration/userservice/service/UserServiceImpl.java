package works.integration.userservice.service;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import works.integration.userservice.entity.User;
import works.integration.userservice.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
