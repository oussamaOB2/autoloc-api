package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Vehicule;

/** Offre WEEKEND : tarif journalier -20 %. */
@Component("WEEKEND")
public class WeekendOffreStrategy implements OffreTarifStrategy {

    @Override
    public double calculer(Vehicule vehicule, int jours) {
        return vehicule.getTarifJournalier().doubleValue() * 0.80 * jours;
    }
}
