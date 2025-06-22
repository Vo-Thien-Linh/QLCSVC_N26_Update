package service;

import model.User;
import repository.ManagerUserRepository;
import utils.PasswordEncryptionUtils;

public class LoginService {
    private final ManagerUserRepository userRepository = new ManagerUserRepository();

    public User authenticateUser(String username, String password) {
        User user = userRepository.getUserByUsername(username);
        if (user != null) {
            String hashedInputPassword = PasswordEncryptionUtils.hashPassword(password);
            System.out.println("Hashed input: " + hashedInputPassword + ", Stored password: " + user.getPassword());
            if (hashedInputPassword != null && hashedInputPassword.equals(user.getPassword())) {
                return user;
            } else {
                System.out.println("Mật khẩu không khớp");
            }
        } else {
            System.out.println("Người dùng không tồn tại");
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }
}