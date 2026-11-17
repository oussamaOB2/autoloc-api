package tn.esprit.autoloc.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 9, étapes 12-18 — mise à jour d'une agence avec ajout d'employés.
 * <p>
 * Classe SANS @Transactional : une transaction au niveau du test masquerait justement le bug
 * d'absence de transaction que l'atelier veut démontrer (étapes 15-17). Le nettoyage se fait
 * dans @AfterEach.
 */
@SpringBootTest
@ActiveProfiles("test")
class AgenceUpdateEmployesTest {

    @Autowired IAgenceService agenceService;
    @Autowired JdbcTemplate jdbc;

    private Long idAgence;

    @BeforeEach
    void setUp() {
        Agence agence = new Agence();
        agence.setNom("Avant-" + UUID.randomUUID().toString().substring(0, 8));
        agence.setVille("Tunis");
        agence = agenceService.create(agence);   // id null requis (check de création)
        idAgence = agence.getIdAgence();
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM employe WHERE agence_id = ?", idAgence);
        jdbc.update("DELETE FROM agence WHERE id_agence = ?", idAgence);
    }

    private Employe employe(String nom) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom("Prenom");
        e.setRole(RoleEmploye.AGENT);
        return e;
    }

    private Agence sourceAvecEmployes(String nom, List<Employe> employes) {
        Agence source = new Agence();
        source.setNom(nom);
        source.setVille("Sfax");
        source.setAdresse("1 rue B9");
        source.setTelephone("74000000");
        source.setEmployes(employes);
        return source;
    }

    private Integer countEmployes() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM employe WHERE agence_id = ?", Integer.class, idAgence);
    }

    private String nomAgence() {
        return jdbc.queryForObject("SELECT nom FROM agence WHERE id_agence = ?", String.class, idAgence);
    }

    private int effectif() {
        return jdbc.queryForObject("SELECT effectif FROM agence WHERE id_agence = ?", Integer.class, idAgence);
    }

    /* --------------------------------------------------------- étapes 12-14 : cas valide */

    @Test
    void updateAvecEmployes_valid_insertsEmployeesAndSetsEffectif() {
        Agence source = sourceAvecEmployes("Apres-Valide",
                List.of(employe("Ben Salah"), employe("Trabelsi")));

        Agence resultat = agenceService.updateAvecEmployes(idAgence, source);

        assertEquals(idAgence, resultat.getIdAgence());
        assertEquals("Apres-Valide", nomAgence(), "champs de l'agence mis à jour");
        assertEquals(2, countEmployes(), "les 2 employés sont insérés");
        assertEquals(2, effectif(), "effectif = taille de la liste");
        // les 2 lignes portent bien la clé étrangère de l'agence (côté propriétaire)
        Integer sansAgence = jdbc.queryForObject(
                "SELECT COUNT(*) FROM employe WHERE agence_id IS NULL AND nom IN ('Ben Salah','Trabelsi')",
                Integer.class);
        assertEquals(0, sansAgence, "aucun employé orphelin");
    }

    /* --------------------------------------------- étapes 15-17 : 2e employé invalide -> rollback */

    @Test
    void updateAvecEmployes_invalidSecondEmployee_rollsBackEverything() {
        Employe invalide = employe(null);   // nom manquant : rejeté par le check de l'employé
        Agence source = sourceAvecEmployes("Apres-Invalide",
                List.of(employe("Ben Salah"), invalide));

        Exception echec = assertThrows(IllegalArgumentException.class,
                () -> agenceService.updateAvecEmployes(idAgence, source));
        assertNotNull(echec.getMessage());   // rejet du 2e employé par le check de EmployeServiceImpl

        // Preuve du rollback : ni employé, ni changement d'agence, ni effectif.
        assertEquals(0, countEmployes(), "AUCUN employé inséré (rollback)");
        assertEquals("Tunis", jdbc.queryForObject(
                "SELECT ville FROM agence WHERE id_agence = ?", String.class, idAgence),
                "ville de l'agence inchangée");
        assertEquals(0, effectif(), "effectif inchangé");
        assertEquals("Avant", nomAgence().substring(0, 5), "nom de l'agence inchangé");
    }

    /* -------------------------------------------------- étapes 17-18 : rejeu avec données valides */

    @Test
    void updateAvecEmployes_afterRollback_validAgain_succeeds() {
        // échec d'abord
        assertThrows(IllegalArgumentException.class, () -> agenceService.updateAvecEmployes(idAgence,
                sourceAvecEmployes("Non", List.of(employe("X"), employe(null)))));

        // puis rejeu valide : tout repart à zéro, tout est inséré
        Agence resultat = agenceService.updateAvecEmployes(idAgence,
                sourceAvecEmployes("Apres-Rejeu", List.of(employe("Ben Salah"), employe("Trabelsi"))));

        assertNotNull(resultat);
        assertEquals("Apres-Rejeu", nomAgence());
        assertEquals(2, countEmployes());
        assertEquals(2, effectif());
    }
}
