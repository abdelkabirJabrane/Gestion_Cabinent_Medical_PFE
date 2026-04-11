package com.shop.user_service.service;

import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.User;

import java.util.List;

public interface UserService {


    User createUser(User user);

    User updateUser(Long id, User user);

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    List<User> getAllUsers();

    void deleteUser(Long id);

    // User management
    User activateUser(Long id);

    User deactivateUser(Long id);

    User lockAccount(Long id);

    User unlockAccount(Long id);

    User changePassword(Long id, String oldPassword, String newPassword);

    User resetPassword(Long id, String newPassword);

    User updateProfile(Long id, User user);

    User verifyEmail(Long id);

    User updateLastLogin(Long id);

    User addRole(Long id, Role role);

    User removeRole(Long id, Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> getActiveUsers();

    List<User> getInactiveUsers();

    List<User> getUsersByRole(Role role);

    List<User> searchUsers(String keyword);

    void deleteAll(List<Long> ids);

    List<User> getSecretairesByMedecinId(Long medecinId);

}
