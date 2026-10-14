package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.service.IEquipementService;

@RestController
@RequestMapping("/api/equipements")
@Tag(name = "Equipements", description = "Gestion des équipements")
public class EquipementController extends AbstractCrudController<Equipement> {

    public EquipementController(IEquipementService service) {
        super(service);
    }
}
