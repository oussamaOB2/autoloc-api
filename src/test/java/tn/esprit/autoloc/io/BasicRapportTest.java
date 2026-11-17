package tn.esprit.autoloc.io;

import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.domain.Agence;

import static org.junit.jupiter.api.Assertions.*;

class BasicRapportTest {

    private Agence agence() {
        Agence a = new Agence();
        a.setIdAgence(1L);
        a.setNom("Tunis");
        a.setTelephone("20000000");
        return a;
    }

    @Test
    void construire_returnsHeaderAndTotal() {
        String texte = new BasicRapport().construire(agence(), 6000.0);
        assertTrue(texte.contains("Agence #1"), texte);
        assertTrue(texte.contains("Tunis"), texte);
        assertTrue(texte.contains("20000000"), texte);
        assertTrue(texte.contains("total tarif:6000.0"), texte);
    }
}
