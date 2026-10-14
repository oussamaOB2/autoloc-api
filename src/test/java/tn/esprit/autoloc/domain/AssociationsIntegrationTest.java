package tn.esprit.autoloc.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssociationsIntegrationTest {

    @Autowired IAgenceRepository agenceRepo;
    @Autowired IVehiculeRepository vehiculeRepo;
    @Autowired IEquipementRepository equipementRepo;
    @Autowired IClientRepository clientRepo;
    @Autowired IReservationRepository reservationRepo;
    @Autowired IContratRepository contratRepo;
    @Autowired IPaiementRepository paiementRepo;
    @Autowired EntityManager em;

    private Vehicule newVehicule(Agence agence) {
        Vehicule v = new Vehicule();
        v.setImmatriculation("TN-" + UUID.randomUUID()); // unique => test can be repeated
        v.setMarque("Peugeot");
        v.setModele("208");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setTarifJournalier(new BigDecimal("80.00"));
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setAgence(agence);
        return v;
    }

    @Test
    void oneToMany_agence_vehicules_isMappedByVehiculeAgence() {
        Agence agence = new Agence();
        agence.setNom("Tunis Centre");
        agence = agenceRepo.save(agence);

        Vehicule v = vehiculeRepo.save(newVehicule(agence));
        em.flush();
        em.clear();

        Agence reloaded = agenceRepo.findById(agence.getIdAgence()).orElseThrow();
        assertEquals(1, reloaded.getVehicules().size());
        assertEquals(v.getIdVehicule(), reloaded.getVehicules().get(0).getIdVehicule());
    }

    @Test
    void manyToMany_vehicule_equipements() {
        Equipement gps = new Equipement();
        gps.setLibelle("GPS");
        gps = equipementRepo.save(gps);

        Vehicule v = newVehicule(null);
        v.getEquipements().add(gps);
        v = vehiculeRepo.save(v);
        em.flush();
        em.clear();

        Vehicule reloaded = vehiculeRepo.findById(v.getIdVehicule()).orElseThrow();
        assertEquals(1, reloaded.getEquipements().size());
        assertEquals("GPS", reloaded.getEquipements().get(0).getLibelle());
    }

    @Test
    void cascadePersist_client_savesItsReservations() {
        Vehicule v = vehiculeRepo.save(newVehicule(null));

        Client client = new Client();
        client.setNom("Ben Ali");
        client.setDateInscription(LocalDate.now());

        Reservation r = new Reservation();
        r.setDateDebut(LocalDate.now());
        r.setDateFin(LocalDate.now().plusDays(3));
        r.setStatut(StatutReservation.EN_ATTENTE);
        r.setVehicule(v);
        r.setClient(client);
        client.getReservations().add(r);

        clientRepo.save(client); // reservation is NOT saved explicitly
        em.flush();

        assertNotNull(r.getIdReservation(), "PERSIST must cascade from Client to Reservation");
        assertEquals(1, reservationRepo.count());
    }

    @Test
    void cascadeAll_deletingContrat_deletesItsPaiements() {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(LocalDate.now());
        contrat.setMontantTotal(new BigDecimal("300.00"));
        contrat.setValide(true);

        Paiement p1 = new Paiement();
        p1.setMontant(new BigDecimal("100.00"));
        p1.setDatePaiement(LocalDate.now());
        p1.setModePaiement(ModePaiement.CARTE);
        p1.setContrat(contrat);

        Paiement p2 = new Paiement();
        p2.setMontant(new BigDecimal("200.00"));
        p2.setDatePaiement(LocalDate.now());
        p2.setModePaiement(ModePaiement.ESPECES);
        p2.setContrat(contrat);

        contrat.getPaiements().addAll(List.of(p1, p2));
        contrat = contratRepo.save(contrat);
        em.flush();
        assertEquals(2, paiementRepo.count());

        contratRepo.delete(contrat);
        em.flush();

        assertEquals(0, paiementRepo.count(), "REMOVE must cascade from Contrat to Paiement");
    }

    @Test
    void noCascadeRemove_deletingAgence_doesNotDeleteVehicules() {
        Agence agence = new Agence();
        agence.setNom("Sfax");
        agence = agenceRepo.save(agence);
        Vehicule v = vehiculeRepo.save(newVehicule(agence));
        em.flush();

        // detach the link first: a vehicle must survive the agency (no remove cascade)
        v.setAgence(null);
        vehiculeRepo.save(v);
        em.flush();
        agenceRepo.deleteById(agence.getIdAgence());
        em.flush();

        assertTrue(vehiculeRepo.findById(v.getIdVehicule()).isPresent());
    }
}
