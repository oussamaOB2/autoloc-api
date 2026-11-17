package tn.esprit.autoloc.io;

import tn.esprit.autoloc.domain.Agence;

/**
 * Atelier 8, étape 14 — Fabrique + Décorateur : construit le texte d'un rapport de simulation.
 * Une seule méthode : reçoit les données d'en-tête de l'agence et le total, retourne le texte.
 */
public interface RapportAgence {

    String construire(Agence agence, double total);
}
