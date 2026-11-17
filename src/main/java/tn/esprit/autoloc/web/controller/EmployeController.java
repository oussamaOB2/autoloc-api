package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.service.IEmployeService;

@RestController
@RequestMapping("/employes")
@Tag(name = "Employes", description = "Gestion des employés")
public class EmployeController extends AbstractCrudController<Employe, Long> {

    public EmployeController(IEmployeService service) {
        super(service);
    }
}
