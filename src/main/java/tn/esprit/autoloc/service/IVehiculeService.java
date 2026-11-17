package tn.esprit.autoloc.service;

import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Vehicule;

public interface IVehiculeService extends IGenericService<Vehicule, Long> {

    double calculTarif(Vehicule vehicule, int jours, VehiculeOffre offre);

    /* Atelier 9 — affectation/désaffectation : seuls ces côtés propriétaires persistant */

    void unassignAgence(Long idVehicule);

    Vehicule assignAgence(Long idVehicule, Long idAgence);

    Vehicule assignEquipement(Long idVehicule, Long idEquipement);
}