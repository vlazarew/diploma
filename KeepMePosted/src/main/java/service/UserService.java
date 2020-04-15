package service;

import data.User;
import data.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findByChatId(long id) {
        return userRepository.findByChatId(id);
    }

    @Transactional
    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Transactional
    public List<User> findNewUsers() {
        List<User> newUsers = userRepository.findNewUsers();
        newUsers.forEach(user -> user.setNotified(true));
        userRepository.saveAll(newUsers);

        return newUsers;
    }

    @Transactional
    public void addUser(User user) {
        user.setIsAdmin(userRepository.count() == 0);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

}
