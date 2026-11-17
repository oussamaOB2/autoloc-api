package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.calcul.LongueDureeOffreStrategy;
import tn.esprit.autoloc.calcul.StandardOffreStrategy;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.calcul.WeekendOffreStrategy;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.io.BasicRapport;
import tn.esprit.autoloc.io.ConsoleRapportDecorator;
import tn.esprit.autoloc.io.RapportFactory;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Gate de régression (atelier 8) : les totaux de {@code simuler} doivent rester identiques
 * après le refactoring SRP (étape 11) et la stratégie Map (étape 16).
 * Fixture : une agence avec deux véhicules (tarif 100 et 500), 10 jours.
 */
class AgenceSimulerRegressionTest {

    private AgenceServiceImpl serviceWithFixture() {
        IAgenceRepository agenceRepo = mock(IAgenceRepository.class);
        IVehiculeRepository vehiculeRepo = mock(IVehiculeRepository.class);

        Agence agence = new Agence();
        agence.setIdAgence(1L);
        agence.setNom("Tunis");
        agence.setTelephone("20000000");

        Vehicule v1 = new Vehicule();
        v1.setTarifJournalier(new BigDecimal("100"));
        Vehicule v2 = new Vehicule();
        v2.setTarifJournalier(new BigDecimal("500"));
        agence.setVehicules(List.of(v1, v2));

        when(agenceRepo.findById(1L)).thenReturn(Optional.of(agence));

        // calculTarif réel (VehiculeServiceImpl) : c'est lui qu'on veut vérer
        BasicRapport basic = new BasicRapport();
        RapportFactory factory = new RapportFactory(basic, new ConsoleRapportDecorator(basic));
        VehiculeServiceImpl vehiculeService = new VehiculeServiceImpl(vehiculeRepo,
                mock(IAgenceRepository.class), mock(IEquipementRepository.class), Map.of(
                "STANDARD", new StandardOffreStrategy(),
                "WEEKEND", new WeekendOffreStrategy(),
                "LONGUE_DUREE", new LongueDureeOffreStrategy()));
        return new AgenceServiceImpl(agenceRepo, vehiculeService, factory);
    }

    @Test
    void simuler_standard_totals6000() {
        String rapport = serviceWithFixture().simuler(1L, 10, VehiculeOffre.STANDARD);
        assertTrue(rapport.contains("total tarif:6000.0"), rapport);
    }

    @Test
    void simuler_weekend_totals4800() {
        String rapport = serviceWithFixture().simuler(1L, 10, VehiculeOffre.WEEKEND);
        assertTrue(rapport.contains("total tarif:4800.0"), rapport);
    }

    @Test
    void simuler_longueDuree_totals4312_5() {
        String rapport = serviceWithFixture().simuler(1L, 10, VehiculeOffre.LONGUE_DUREE);
        // 100 : 750 (<=2000, pas de -5%) + 500 : 3750 > 2000 -> x0.95 = 3562.5
        assertTrue(rapport.contains("total tarif:4312.5"), rapport);
    }

    @Test
    void simuler_containsAgencyHeader() {
        String rapport = serviceWithFixture().simuler(1L, 10, VehiculeOffre.STANDARD);
        assertTrue(rapport.contains("Agence #1"), rapport);
        assertTrue(rapport.contains("Tunis"), rapport);
    }

    @Test
    void simuler_invalidDaysOrOffer_throwsIllegalArgument() {
        AgenceServiceImpl service = serviceWithFixture();
        assertThrows(IllegalArgumentException.class, () -> service.simuler(1L, 0, VehiculeOffre.STANDARD));
        assertThrows(IllegalArgumentException.class, () -> service.simuler(1L, 10, null));
    }

    @Test
    void simuler_unknownAgency_throwsResourceNotFound() {
        assertThrows(tn.esprit.autoloc.exception.ResourceNotFoundException.class,
                () -> serviceWithFixture().simuler(99L, 10, VehiculeOffre.STANDARD));
    }
}
