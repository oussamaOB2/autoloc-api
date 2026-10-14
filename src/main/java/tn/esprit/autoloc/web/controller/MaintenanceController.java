package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.service.IMaintenanceService;

@RestController
@RequestMapping("/api/maintenances")
@Tag(name = "Maintenances", description = "Gestion des maintenances")
public class MaintenanceController extends AbstractCrudController<Maintenance> {

    public MaintenanceController(IMaintenanceService service) {
        super(service);
    }
}
