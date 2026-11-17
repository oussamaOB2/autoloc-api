package tn.esprit.autoloc.service;

import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;

public interface IAgenceService extends IGenericService<Agence, Long> {

    boolean hasVehicules(Long id);

    String simuler(Long idAgence, int jours, VehiculeOffre offre);

    /**
     * Atelier 9, étape 12 : met à jour l'agence puis ajoute la liste d'employés reçue
     * (chaque employé est créé via le service employé et relié par la symétrie addEmploye),
     * et fixe {@code effectif} à la taille de la liste.
     */
    Agence updateAvecEmployes(Long idAgence, Agence source);
}