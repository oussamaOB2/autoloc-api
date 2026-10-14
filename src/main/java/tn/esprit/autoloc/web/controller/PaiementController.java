package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.service.IPaiementService;

@RestController
@RequestMapping("/api/paiements")
@Tag(name = "Paiements", description = "Gestion des paiements")
public class PaiementController extends AbstractCrudController<Paiement> {

    public PaiementController(IPaiementService service) {
        super(service);
    }
}
