package tn.esprit.autoloc.calcul;

import tn.esprit.autoloc.domain.Vehicule;

/**
 * Atelier 8, étape 16 — Stratégie d'offre de tarification (OCP) : chaque offre est une classe
 * {@code @Component} nommée comme la constante de l'enum ; plus de {@code switch} dans le service.
 */
public interface OffreTarifStrategy {

    double calculer(Vehicule vehicule, int jours);
}
