package com.shop.user_service.repository;


import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================
    // FINDERS
    // =========================
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();


    // =========================
    // SEARCH
    // =========================
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    // =========================
    // EXISTS
    // =========================
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // =========================
    // COUNT
    // =========================
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    Long countActiveUsers();


    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.medecinId = :medecinId AND 'ROLE_SECRETAIRE' MEMBER OF u.roles")
    List<User> findSecretairesByMedecinId(@Param("medecinId") Long medecinId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.medecinId = :medecinId AND 'ROLE_SECRETAIRE' MEMBER OF u.roles")
    long countSecretairesByMedecinId(@Param("medecinId") Long medecinId);

}
