package tn.esprit.autoloc.calcul;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Vehicule;

/**
 * Offre LONGUE_DUREE : tarif journalier -25 %, puis -5 % de plus si le total du véhicule
 * dépasse 2000 (le seuil s'applique au total déjà réduit de 25 %).
 */
@Component("LONGUE_DUREE")
public class LongueDureeOffreStrategy implements OffreTarifStrategy {

    @Override
    public double calculer(Vehicule vehicule, int jours) {
        double total = vehicule.getTarifJournalier().doubleValue() * 0.75 * jours;
        if (total > 2000) {
            total = total * 0.95;
        }
        return total;
    }
}
