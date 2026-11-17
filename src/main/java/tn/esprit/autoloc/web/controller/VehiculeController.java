package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.IVehiculeService;

@RestController
@RequestMapping("/vehicules")
@Tag(name = "Vehicules", description = "Gestion des véhicules")
public class VehiculeController extends AbstractCrudController<Vehicule, Long> {

    private final IVehiculeService vehiculeService;

    public VehiculeController(IVehiculeService service) {
        super(service);
        this.vehiculeService = service;
    }

    /* Atelier 9, partie I — affectation/désaffectation */

    @DeleteMapping("/{id}/agence")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignAgence(@PathVariable Long id) {
        vehiculeService.unassignAgence(id);
    }

    @PutMapping("/{idVehicule}/agence/{idAgence}")
    public Vehicule assignAgence(@PathVariable Long idVehicule, @PathVariable Long idAgence) {
        return vehiculeService.assignAgence(idVehicule, idAgence);
    }

    @PutMapping("/{idVehicule}/equipements/{idEquipement}")
    public Vehicule assignEquipement(@PathVariable Long idVehicule, @PathVariable Long idEquipement) {
        return vehiculeService.assignEquipement(idVehicule, idEquipement);
    }
}
