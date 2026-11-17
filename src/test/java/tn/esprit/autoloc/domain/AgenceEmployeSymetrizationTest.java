package tn.esprit.autoloc.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 9, étape 10 — symétrisation affectation/désaffectation au sein des entités :
 * les deux côtés de la relation bidirectionnelle Agence ↔ Employe sont mis à jour ensemble
 * (les collections sont déjà initialisées ; les entités n'ont ni @Data ni
 * toString/equals/hashCode sur les collections — pas de cycle à exclure).
 */
class AgenceEmployeSymetrizationTest {

    @Test
    void addEmploye_setsBothSides() {
        Agence agence = new Agence();
        agence.setNom("Tunis");
        Employe employe = new Employe();
        employe.setNom("Ben Salah");

        agence.addEmploye(employe);

        assertTrue(agence.getEmployes().contains(employe), "ajouté dans la collection de l'agence");
        assertSame(agence, employe.getAgence(), "côté propriétaire (employe.agence) pointe vers l'agence");
    }

    @Test
    void removeEmploye_clearsBothSides() {
        Agence agence = new Agence();
        agence.setNom("Tunis");
        Employe employe = new Employe();
        employe.setNom("Ben Salah");
        agence.addEmploye(employe);

        agence.removeEmploye(employe);

        assertFalse(agence.getEmployes().contains(employe), "retiré de la collection de l'agence");
        assertNull(employe.getAgence(), "côté propriétaire désaffecté (set null)");
    }

    @Test
    void employeCollection_isInitializedByDefault() {
        assertNotNull(new Agence().getEmployes());
        assertNotNull(new Agence().getVehicules());
        assertNotNull(new Equipement().getVehicules());
        assertNotNull(new Vehicule().getEquipements());
    }
}
