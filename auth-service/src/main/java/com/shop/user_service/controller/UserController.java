package com.shop.user_service.controller;

import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.User;
import com.shop.user_service.service.UserService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final MeterRegistry meterRegistry;

    // Metrics business
    private final Counter userCreatedCounter;
    private final Counter userDeletedCounter;
    private final Counter userActivatedCounter;
    private final Counter userDeactivatedCounter;
    private final Counter userLockedCounter;

    public UserController(UserService userService, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.meterRegistry = meterRegistry;

        this.userCreatedCounter = Counter.builder("users.created")
                .description("Nombre total d'utilisateurs créés")
                .register(meterRegistry);

        this.userDeletedCounter = Counter.builder("users.deleted")
                .description("Nombre total d'utilisateurs supprimés")
                .register(meterRegistry);

        this.userActivatedCounter = Counter.builder("users.activated")
                .description("Nombre total d'utilisateurs activés")
                .register(meterRegistry);

        this.userDeactivatedCounter = Counter.builder("users.deactivated")
                .description("Nombre total d'utilisateurs désactivés")
                .register(meterRegistry);

        this.userLockedCounter = Counter.builder("users.locked")
                .description("Nombre total d'utilisateurs verrouillés")
                .register(meterRegistry);
    }

    // ========================= CREATE =========================

    @PostMapping
    @Timed(value = "users.create", description = "Temps de création d'un utilisateur")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("Creating user: {}", user.getUsername());
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            User createdUser = userService.createUser(user);
            userCreatedCounter.increment();

            sample.stop(Timer.builder("users.create.success")
                    .register(meterRegistry));

            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

        } catch (Exception e) {
            sample.stop(Timer.builder("users.create.failed")
                    .register(meterRegistry));

            log.error("Failed to create user: {}", user.getUsername(), e);
            throw e;
        }
    }

    // ========================= READ =========================

    @GetMapping("/{id}")
    @Timed(value = "users.get.by.id", description = "Temps récupération utilisateur par ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Timed(value = "users.get.by.username", description = "Temps récupération utilisateur par username")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        log.info("Fetching user by username: {}", username);
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    @Timed(value = "users.get.by.email", description = "Temps récupération utilisateur par email")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user by email: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    @Timed(value = "users.list", description = "Temps récupération tous les utilisateurs")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/secretaires/by-medecin/{medecinId}
     * Récupère les secrétaires d'un médecin donné
     */
    @GetMapping("/secretaires/by-medecin/{medecinId}")
    public ResponseEntity<List<User>> getSecretairesByMedecin(@PathVariable Long medecinId) {
        log.info("Fetching secretaires for medecin {}", medecinId);
        List<User> secretaires = userService.getSecretairesByMedecinId(medecinId);
        return ResponseEntity.ok(secretaires);
    }

    // ========================= UPDATE =========================

    @PutMapping("/{id}")
    @Timed(value = "users.update", description = "Temps de mise à jour utilisateur")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user
    ) {
        log.info("Updating user {}", id);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // ========================= DELETE =========================

    @DeleteMapping("/{id}")
    @Timed(value = "users.delete", description = "Temps de suppression utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user {}", id);
        userService.deleteUser(id);
        userDeletedCounter.increment();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Timed(value = "users.delete.batch", description = "Temps de suppression en lot")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        log.info("Deleting {} users", ids.size());
        userService.deleteAll(ids);
        userDeletedCounter.increment(ids.size());
        return ResponseEntity.noContent().build();
    }

    // ================= STATUS =================

    @PutMapping("/{id}/activate")
    @Timed(value = "users.activate", description = "Temps d'activation utilisateur")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        log.info("Activating user {}", id);
        User user = userService.activateUser(id);
        userActivatedCounter.increment();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/deactivate")
    @Timed(value = "users.deactivate", description = "Temps de désactivation utilisateur")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user {}", id);
        User user = userService.deactivateUser(id);
        userDeactivatedCounter.increment();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/lock")
    @Timed(value = "users.lock", description = "Temps de verrouillage compte")
    public ResponseEntity<User> lockAccount(@PathVariable Long id) {
        log.info("Locking account {}", id);
        User user = userService.lockAccount(id);
        userLockedCounter.increment();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/unlock")
    @Timed(value = "users.unlock", description = "Temps de déverrouillage compte")
    public ResponseEntity<User> unlockAccount(@PathVariable Long id) {
        log.info("Unlocking account {}", id);
        return ResponseEntity.ok(userService.unlockAccount(id));
    }

    // ================= PASSWORD =================

    @PutMapping("/{id}/change-password")
    @Timed(value = "users.change.password", description = "Temps de changement mot de passe")
    public ResponseEntity<User> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        log.info("Changing password for user {}", id);
        return ResponseEntity.ok(userService.changePassword(id, oldPassword, newPassword));
    }

    @PutMapping("/{id}/reset-password")
    @Timed(value = "users.reset.password", description = "Temps de réinitialisation mot de passe")
    public ResponseEntity<User> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword
    ) {
        log.info("Resetting password for user {}", id);
        return ResponseEntity.ok(userService.resetPassword(id, newPassword));
    }

    // ================= ROLE =================

    @PutMapping("/{id}/roles/add")
    @Timed(value = "users.add.role", description = "Temps d'ajout de rôle")
    public ResponseEntity<User> addRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        log.info("Adding role {} to user {}", role, id);
        return ResponseEntity.ok(userService.addRole(id, role));
    }

    @PutMapping("/{id}/roles/remove")
    @Timed(value = "users.remove.role", description = "Temps de suppression de rôle")
    public ResponseEntity<User> removeRole(
            @PathVariable Long id,
            @RequestParam Role role
    ) {
        log.info("Removing role {} from user {}", role, id);
        return ResponseEntity.ok(userService.removeRole(id, role));
    }

    // ========================= HEALTH =========================

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is UP");
    }
}