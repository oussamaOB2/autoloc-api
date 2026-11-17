package tn.esprit.autoloc.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.autoloc.domain.*;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 9, partie I (étapes 1-9) — affectation/désaffectation sur le côté propriétaire.
 * Classe SANS @Transactional : on veut voir ce qui persiste réellement (dirty checking dans la
 * transaction du service) et les vraies violations de contrainte au commit.
 */
@SpringBootTest
@ActiveProfiles("test")
class VehiculeAffectationTest {

    @Autowired IVehiculeService vehiculeService;
    @Autowired IAgenceRepository agenceRepo;
    @Autowired IVehiculeRepository vehiculeRepo;
    @Autowired IEquipementRepository equipementRepo;
    @Autowired IClientRepository clientRepo;
    @Autowired IReservationRepository reservationRepo;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;

    private Agence agence;
    private Vehicule vehicule;
    private Equipement equipement;

    @BeforeEach
    void setUp() {
        agence = new Agence();
        agence.setNom("Agence-" + UUID.randomUUID().toString().substring(0, 8));
        agence = agenceRepo.save(agence);

        vehicule = newVehicule(agence);
        vehicule = vehiculeRepo.save(vehicule);

        equipement = new Equipement();
        equipement.setLibelle("Equip-" + UUID.randomUUID().toString().substring(0, 8));
        equipement = equipementRepo.save(equipement);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM reservation");       // pas de table liée aux tests si vide
        jdbc.update("DELETE FROM vehicule_equipement");
        jdbc.update("DELETE FROM vehicule");
        jdbc.update("DELETE FROM equipement");
        jdbc.update("DELETE FROM employe");
        jdbc.update("DELETE FROM agence");
        jdbc.update("DELETE FROM client");
    }

    private Vehicule newVehicule(Agence a) {
        Vehicule v = new Vehicule();
        v.setImmatriculation("TN-" + UUID.randomUUID());
        v.setMarque("Peugeot");
        v.setModele("208");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setTarifJournalier(new BigDecimal("80.00"));
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setAgence(a);
        return v;
    }

    private Long vehiculeAgenceId(Long idVehicule) {
        return jdbc.queryForObject("SELECT agence_id FROM vehicule WHERE id_vehicule = ?", Long.class, idVehicule);
    }

    /* -------------------------------------------------- étapes 1-3 : désaffecter l'agence */

    @Test
    void unassignAgence_setsOwnerSideNullAndKeepsAgenceRow() {
        vehiculeService.unassignAgence(vehicule.getIdVehicule());

        assertNull(vehiculeAgenceId(vehicule.getIdVehicule()), "agence_id doit être NULL en base");
        assertTrue(agenceRepo.findById(agence.getIdAgence()).isPresent(), "la ligne agence existe toujours");
        em.clear();
        assertNull(vehiculeRepo.findById(vehicule.getIdVehicule()).orElseThrow().getAgence(),
                "GET /vehicules/{id} rendrait agence null");
    }

    @Test
    void unassignAgence_unknownVehicle_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.unassignAgence(999999L));
    }

    /* --------------------------------------------------- étapes 4-5 : affecter une agence */

    @Test
    void assignAgence_persistsOnOwnerSide() {
        Agence autre = new Agence();
        autre.setNom("Autre-" + UUID.randomUUID().toString().substring(0, 8));
        autre = agenceRepo.save(autre);

        Vehicule resultat = vehiculeService.assignAgence(vehicule.getIdVehicule(), autre.getIdAgence());

        assertEquals(autre.getIdAgence(), resultat.getAgence().getIdAgence());
        assertEquals(autre.getIdAgence(), vehiculeAgenceId(vehicule.getIdVehicule()),
                "le côté propriétaire (agence_id) est bien persisté");
        em.clear();
        assertEquals(autre.getIdAgence(),
                vehiculeRepo.findById(vehicule.getIdVehicule()).orElseThrow().getAgence().getIdAgence());
    }

    @Test
    void assignAgence_unknownVehicle_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.assignAgence(999999L, agence.getIdAgence()));
    }

    @Test
    void assignAgence_unknownAgence_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.assignAgence(vehicule.getIdVehicule(), 999999L));
    }

    /* ----------------------------------------------- étapes 6-7 : affecter un équipement */

    @Test
    void assignEquipement_addsJoinTableRowOnOwnerSide() {
        vehiculeService.assignEquipement(vehicule.getIdVehicule(), equipement.getIdEquipement());

        Integer liens = jdbc.queryForObject(
                "SELECT COUNT(*) FROM vehicule_equipement WHERE vehicule_id = ? AND equipement_id = ?",
                Integer.class, vehicule.getIdVehicule(), equipement.getIdEquipement());
        assertEquals(1, liens, "la ligne de jointure doit exister");
    }

    @Test
    void assignEquipement_calledTwice_noDuplicate() {
        vehiculeService.assignEquipement(vehicule.getIdVehicule(), equipement.getIdEquipement());
        vehiculeService.assignEquipement(vehicule.getIdVehicule(), equipement.getIdEquipement());

        Integer liens = jdbc.queryForObject("SELECT COUNT(*) FROM vehicule_equipement",
                Integer.class);
        assertEquals(1, liens, "pas de doublon dans la table de jointure");
    }

    @Test
    void assignEquipement_unknownEquipement_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.assignEquipement(vehicule.getIdVehicule(), 999999L));
    }

    /* ------------------------------------------- étape 8 : suppression véhicule lié */

    @Test
    void delete_vehicleWithEquipement_removesJoinRowsAndSucceeds() {
        vehiculeService.assignEquipement(vehicule.getIdVehicule(), equipement.getIdEquipement());

        // OBSERVÉ (étape 8) : aucune exception. Vehicule est le côté PROPRIÉTAIRE du @ManyToMany
        // (@JoinTable vehicule_equipement) : Hibernate supprime d'abord les lignes de jointure
        // qu'il gère, puis la ligne vehicule. Le support annonçait un échec → voir rapport
        // « Incohérences du support ». Aucune mapping n'est changé pour forcer l'erreur.
        assertDoesNotThrow(() -> vehiculeService.deleteById(vehicule.getIdVehicule()));

        assertTrue(vehiculeRepo.findById(vehicule.getIdVehicule()).isEmpty(), "le véhicule est supprimé");
        Integer liens = jdbc.queryForObject("SELECT COUNT(*) FROM vehicule_equipement", Integer.class);
        assertEquals(0, liens, "Hibernate a nettoyé les lignes de jointure du propriétaire");
        assertTrue(equipementRepo.findById(equipement.getIdEquipement()).isPresent(),
                "l'équipement (côté inverse) est intact");
    }

    @Test
    void delete_vehicleWithReservation_failsWithConstraintViolation() {
        Client client = new Client();
        client.setNom("Ben Salah");
        client.setPrenom("Ali");
        client.setEmail("ali." + UUID.randomUUID().toString().substring(0, 8) + "@test.tn");
        client = clientRepo.save(client);

        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setVehicule(vehicule);
        reservation.setDateDebut(LocalDate.now().plusDays(1));
        reservation.setDateFin(LocalDate.now().plusDays(3));
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservationRepo.save(reservation);

        // Vehicule n'a aucune collection réservations, aucune cascade REMOVE côté Reservation.
        assertThrows(DataIntegrityViolationException.class,
                () -> vehiculeService.deleteById(vehicule.getIdVehicule()));
        assertTrue(vehiculeRepo.findById(vehicule.getIdVehicule()).isPresent());
    }

    /* -------------------------------------------- étape 9 : suppression équipement lié */

    @Test
    void delete_equipementWithLinkedVehicule_failsOnJoinTableForeignKey() {
        vehiculeService.assignEquipement(vehicule.getIdVehicule(), equipement.getIdEquipement());

        // OBSERVÉ (étape 9) : ÉCHEC, contrairement au support (« aucun problème »). Equipement est
        // le côté INVERSE (mappedBy = "equipements") : Hibernate ne touche pas vehicule_equipement,
        // le SGBD refuse la suppression à cause de la FK EQUIPEMENT_ID. C'est bien la table de
        // jointure — pilotée par le propriétaire — qui décide.
        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () -> {
            equipementRepo.deleteById(equipement.getIdEquipement());
            equipementRepo.flush();
        });
        assertTrue(ex.getMessage().toUpperCase().contains("VEHICULE_EQUIPEMENT"), ex.getMessage());
        assertTrue(equipementRepo.findById(equipement.getIdEquipement()).isPresent());
        assertTrue(vehiculeRepo.findById(vehicule.getIdVehicule()).isPresent());
    }
}
