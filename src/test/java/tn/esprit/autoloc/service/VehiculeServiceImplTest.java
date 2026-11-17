package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.calcul.*;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Atelier 8, étape 16 — Stratégie par Map : les tarifs sont portés par des @Component
 * nommés comme les constantes de l'enum ; ajouter une offre = ajouter une classe.
 */
class VehiculeServiceImplTest {

    private static Map<String, OffreTarifStrategy> realStrategies() {
        return Map.of(
                "STANDARD", new StandardOffreStrategy(),
                "WEEKEND", new WeekendOffreStrategy(),
                "LONGUE_DUREE", new LongueDureeOffreStrategy());
    }

    private VehiculeServiceImpl service(Map<String, OffreTarifStrategy> strategies) {
        return new VehiculeServiceImpl(mock(IVehiculeRepository.class), strategies);
    }

    private Vehicule vehicule(String tarif) {
        Vehicule v = new Vehicule();
        v.setTarifJournalier(new BigDecimal(tarif));
        return v;
    }

    @Test
    void calculTarif_standard_fullRate() {
        double total = service(realStrategies()).calculTarif(vehicule("100"), 10, VehiculeOffre.STANDARD);
        assertEquals(1000.0, total, 1e-9);
    }

    @Test
    void calculTarif_weekend_minus20Percent() {
        double total = service(realStrategies()).calculTarif(vehicule("500"), 10, VehiculeOffre.WEEKEND);
        assertEquals(4000.0, total, 1e-9);   // 500 x 0.80 x 10
    }

    @Test
    void calculTarif_longueDuree_below2000_noExtraDiscount() {
        double total = service(realStrategies()).calculTarif(vehicule("100"), 10, VehiculeOffre.LONGUE_DUREE);
        assertEquals(750.0, total, 1e-9);    // 100 x 0.75 x 10, pas de -5 %
    }

    @Test
    void calculTarif_longueDuree_above2000_minus5Percent() {
        double total = service(realStrategies()).calculTarif(vehicule("500"), 10, VehiculeOffre.LONGUE_DUREE);
        assertEquals(3562.5, total, 1e-9);   // 3750 x 0.95
    }

    @Test
    void calculTarif_longueDuree_thresholdOnBothSides() {
        var svc = service(realStrategies());
        // 0.75 x 266 x 10 = 1995.00  (<= 2000 : pas de -5 %)
        assertEquals(1995.0, svc.calculTarif(vehicule("266"), 10, VehiculeOffre.LONGUE_DUREE), 1e-9);
        // 0.75 x 267 x 10 = 2002.50  (> 2000 : x 0.95 = 1902.375)
        assertEquals(2002.5 * 0.95, svc.calculTarif(vehicule("267"), 10, VehiculeOffre.LONGUE_DUREE), 1e-9);
    }

    @Test
    void calculTarif_missingStrategy_throwsClearException() {
        Map<String, OffreTarifStrategy> incomplete = new HashMap<>(realStrategies());
        incomplete.remove("LONGUE_DUREE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service(incomplete).calculTarif(vehicule("100"), 10, VehiculeOffre.LONGUE_DUREE));
        assertTrue(ex.getMessage().contains("LONGUE_DUREE"), ex.getMessage());
    }

    @Test
    void calculTarif_nullOffer_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service(realStrategies()).calculTarif(vehicule("100"), 10, null));
    }

    @Test
    void calculTarif_testOnlyStrategy_isUsedWithoutTouchingTheService() {
        // Preuve OCP : une stratégie inconnue du code de production suffit, aucun switch touché.
        Map<String, OffreTarifStrategy> withTestOffer = new HashMap<>(realStrategies());
        withTestOffer.put("WEEKEND", (vehicule, jours) -> 42.0);

        assertEquals(42.0, service(withTestOffer).calculTarif(vehicule("500"), 10, VehiculeOffre.WEEKEND), 1e-9);
    }
}
