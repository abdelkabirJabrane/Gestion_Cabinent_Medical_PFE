package com.shop.user_service.service;


import com.shop.user_service.config.UserAlreadyExistsException;
import com.shop.user_service.config.UserNotFoundException;
import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.Cabinet;
import com.shop.user_service.entity.User;
import com.shop.user_service.repository.CabinetRepository;
import com.shop.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CabinetRepository cabinetRepository;


    @Override
    public User createUser(User user) {

        log.info("Creating user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(Role.ROLE_PATIENT));
        }

        user.setActive(user.getActive() != null ? user.getActive() : true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);

        user.setAddress(user.getAddress());

        // Créer un cabinet par défaut si l'utilisateur est un MEDECIN et n'a pas de tenantId
        if (user.getRoles().contains(Role.ROLE_MEDECIN) && user.getTenantId() == null) {
            log.info("Création automatique d'un cabinet pour le nouveau médecin : {}", user.getLastName());
            Cabinet defaultCabinet = Cabinet.builder()
                    .nom("Cabinet " + user.getLastName())
                    .responsable(user.getFirstName() + " " + user.getLastName())
                    .email(user.getEmail())
                    .telephone(user.getPhoneNumber())
                    .statut("essai")
                    .nbMedecins(1)
                    .plan("Starter")
                    .dateExpiration(java.time.LocalDate.now().plusDays(14))
                    .build();
            defaultCabinet = cabinetRepository.save(defaultCabinet);
            user.setTenantId(defaultCabinet.getId());
        }

        // Logique spéciale SECRETAIRE : hérite du tenantId du médecin et limité à 2
        if (user.getRoles().contains(Role.ROLE_SECRETAIRE)) {
            Long medecinId = user.getMedecinId();
            if (medecinId == null) {
                throw new IllegalArgumentException("Un compte secrétaire doit être associé à un médecin (medecinId requis)");
            }
            // Récupérer le médecin pour hériter du tenantId
            User medecin = userRepository.findById(medecinId)
                    .orElseThrow(() -> new UserNotFoundException("Médecin introuvable avec ID: " + medecinId));

            // Vérifier que l'utilisateur est bien un médecin
            if (!medecin.getRoles().contains(Role.ROLE_MEDECIN)) {
                throw new IllegalArgumentException("L'identifiant fourni ne correspond pas à un médecin");
            }

            // Vérifier la limite de 2 secrétaires par médecin
            long nbSecretaires = userRepository.countSecretairesByMedecinId(medecinId);
            if (nbSecretaires >= 2) {
                throw new IllegalStateException("Ce médecin a déjà atteint la limite de 2 secrétaires");
            }

            // Hériter du tenantId du médecin
            user.setTenantId(medecin.getTenantId());
            log.info("Secrétaire créée pour le médecin {} avec tenantId={}", medecinId, medecin.getTenantId());
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {

        User existing = getUserById(id);

        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setGender(user.getGender());
        existing.setProfileImageUrl(user.getProfileImageUrl());


        existing.setAddress(user.getAddress());

        if (!existing.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + user.getEmail());
            }
            existing.setEmail(user.getEmail());
            existing.setEmailVerified(false);
        }

        return userRepository.save(existing);
    }











    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);


        userRepository.delete(user);
    }




    @Override
    public User activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    public User deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        return userRepository.save(user);
    }

    @Override
    public User lockAccount(Long id) {
        User user = getUserById(id);
        user.setAccountLocked(true);
        return userRepository.save(user);
    }

    @Override
    public User unlockAccount(Long id) {
        User user = getUserById(id);
        user.setAccountLocked(false);
        return userRepository.save(user);
    }

    @Override
    public User changePassword(Long id, String oldPassword, String newPassword) {

        User user = getUserById(id);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Override
    public User resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Override
    public User updateProfile(Long id, User user) {
        return null;
    }

    @Override
    public User verifyEmail(Long id) {
        User user = getUserById(id);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    @Override
    public User updateLastLogin(Long id) {
        User user = getUserById(id);
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User addRole(Long id, Role role) {

        User user = getUserById(id);

        if (user.getRoles().contains(role)) {
            return user; // déjà présent
        }

        user.addRole(role);
        return userRepository.save(user);
    }

    @Override
    public User removeRole(Long id, Role role) {

        User user = getUserById(id);

        if (!user.getRoles().contains(role)) {
            return user;
        }

        if (user.getRoles().size() == 1) {
            throw new IllegalStateException("User must have at least one role");
        }

        user.removeRole(role);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getInactiveUsers() {
        return userRepository.findByActiveFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role); // plus de findByRolesContaining
    }


    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {
        ids.forEach(this::deleteUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getSecretairesByMedecinId(Long medecinId) {
        return userRepository.findSecretairesByMedecinId(medecinId);
    }
}

