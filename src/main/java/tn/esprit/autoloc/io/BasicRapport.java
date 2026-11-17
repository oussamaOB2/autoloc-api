package tn.esprit.autoloc.io;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Agence;

/**
 * Rapport de base : en-tête + « total tarif: ». Comportement historique de {@code simuler}.
 */
@Component
public class BasicRapport implements RapportAgence {

    /** En-tête partagé avec {@link ConsoleRapportDecorator} (format identique à l'ancien simuler). */
    static String entete(Agence agence) {
        return String.format("Agence #%d - %s - Tél : %s%n",
                agence.getIdAgence(), agence.getNom(), agence.getTelephone());
    }

    @Override
    public String construire(Agence agence, double total) {
        StringBuilder texte = new StringBuilder();
        texte.append(entete(agence));
        texte.append("total tarif:" + total + "\n");
        return texte.toString();
    }
}
