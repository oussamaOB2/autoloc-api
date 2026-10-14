package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.IVehiculeService;

@RestController
@RequestMapping("/api/vehicules")
@Tag(name = "Vehicules", description = "Gestion des véhicules")
public class VehiculeController extends AbstractCrudController<Vehicule> {

    public VehiculeController(IVehiculeService service) {
        super(service);
    }
}
