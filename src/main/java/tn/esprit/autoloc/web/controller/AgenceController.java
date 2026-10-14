package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.service.IAgenceService;

@RestController
@RequestMapping("/api/agences")
@Tag(name = "Agences", description = "Gestion des agences")
public class AgenceController extends AbstractCrudController<Agence> {

    public AgenceController(IAgenceService service) {
        super(service);
    }
}
