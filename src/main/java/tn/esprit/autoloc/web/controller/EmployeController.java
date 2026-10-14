package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.service.ICrudService;

@RestController
@RequestMapping("/api/employes")
@Tag(name = "Employes", description = "Gestion des employés")
public class EmployeController extends AbstractCrudController<Employe> {

    public EmployeController(ICrudService.IEmployeService service) {
        super(service);
    }
}
