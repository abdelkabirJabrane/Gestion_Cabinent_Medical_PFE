package com.shop.user_service.config;

import com.shop.user_service.entity.Plan;
import com.shop.user_service.entity.Role;
import com.shop.user_service.entity.User;
import com.shop.user_service.repository.PlanRepository;
import com.shop.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initialise un compte SUPER_ADMIN au démarrage de l'application
 * pour permettre de tester l'authentification depuis le frontend.
 *
 * ╔══════════════════════════════════════════════╗
 * ║  Identifiants ADMIN de test                 ║
 * ║  ─────────────────────────────────────────── ║
 * ║  Username : admin                           ║
 * ║  Password : Admin@2025                      ║
 * ╚══════════════════════════════════════════════╝
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 Initialisation des comptes de test...");

        // 1. Compte SUPER_ADMIN
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@medicabpro.ma")
                    .password(passwordEncoder.encode("Admin@2025"))
                    .firstName("Super")
                    .lastName("Admin")
                    .phoneNumber("0600000000")
                    .gender("MALE")
                    .address("Casablanca, Maroc")
                    .roles(Set.of(Role.ROLE_SUPER_ADMIN))
                    .active(true)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            userRepository.save(admin);
            log.info("✅ Compte SUPER_ADMIN créé (admin / Admin@2025)");
        }

        // 2. Compte MEDECIN
        if (!userRepository.existsByUsername("medecin")) {
            User medecin = User.builder()
                    .username("medecin")
                    .email("medecin@medicabpro.ma")
                    .password(passwordEncoder.encode("Medecin@2025"))
                    .firstName("Dr. Ahmed")
                    .lastName("Alami")
                    .phoneNumber("0611111111")
                    .gender("MALE")
                    .address("Rabat, Maroc")
                    .roles(Set.of(Role.ROLE_MEDECIN))
                    .active(true)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            userRepository.save(medecin);
            log.info("✅ Compte MEDECIN créé (medecin / Medecin@2025)");
        }

        // 3. Compte SECRETAIRE
        if (!userRepository.existsByUsername("secretaire")) {
            User secretaire = User.builder()
                    .username("secretaire")
                    .email("secretaire@medicabpro.ma")
                    .password(passwordEncoder.encode("Secretaire@2025"))
                    .firstName("Sanaa")
                    .lastName("Bennani")
                    .phoneNumber("0622222222")
                    .gender("FEMALE")
                    .address("Casablanca, Maroc")
                    .roles(Set.of(Role.ROLE_SECRETAIRE))
                    .active(true)
                    .emailVerified(true)
                    .accountLocked(false)
                    .build();
            userRepository.save(secretaire);
            log.info("✅ Compte SECRETAIRE créé (secretaire / Secretaire@2025)");
        }

        // 4. Plans SaaS par défaut
        if (planRepository.count() == 0) {
            planRepository.save(Plan.builder()
                    .planId("Starter")
                    .label("Starter")
                    .prix(299)
                    .color("#10b981")
                    .features("1-3 médecins\n500 RDV/mois\n5 GB stockage\nSupport email")
                    .build());
            planRepository.save(Plan.builder()
                    .planId("Pro")
                    .label("Professionnel")
                    .prix(799)
                    .color("#6366f1")
                    .features("4-10 médecins\n2000 RDV/mois\n50 GB stockage\nSmart AI inclus")
                    .build());
            planRepository.save(Plan.builder()
                    .planId("Enterprise")
                    .label("Entreprise")
                    .prix(null)
                    .color("#a855f7")
                    .features("Illimité\nRDV illimités\n500 GB stockage\nSupport 24/7")
                    .build());
            log.info("✅ Plans SaaS initialisés (Starter / Pro / Enterprise)");
        }

        log.info("══════════════════════════════════════════════════");
        log.info("🚀 Initialisation terminée avec succès !");
        log.info("══════════════════════════════════════════════════");
    }
}
