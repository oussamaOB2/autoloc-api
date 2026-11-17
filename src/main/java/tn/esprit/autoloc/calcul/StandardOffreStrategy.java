package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Vehicule;

/** Offre STANDARD : tarif journalier plein. */
@Component("STANDARD")
public class StandardOffreStrategy implements OffreTarifStrategy {

    @Override
    public double calculer(Vehicule vehicule, int jours) {
        return vehicule.getTarifJournalier().doubleValue() * jours;
    }
}
