package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.service.IAgenceService;

@RestController
@RequestMapping("/agences")
@Tag(name = "Agences", description = "Gestion des agences")
public class AgenceController extends AbstractCrudController<Agence, Long> {

    private final IAgenceService agenceService;                  // NEW: the field

    public AgenceController(IAgenceService service) {
        super(service);
        this.agenceService = service;                            // NEW: the assignment
    }

    @GetMapping(value = "/{id}/simulation", produces = MediaType.TEXT_PLAIN_VALUE)   // NEW
    public String simuler(@PathVariable Long id,
                          @RequestParam int jours,
                          @RequestParam VehiculeOffre offre) {
        return agenceService.simuler(id, jours, offre);
    }

    // Atelier 9, étape 12 : mise à jour d'une agence avec ajout d'employés (étapes 12-18)
    @PutMapping("/{id}/employes")
    public Agence updateAvecEmployes(@PathVariable Long id, @RequestBody Agence source) {
        return agenceService.updateAvecEmployes(id, source);
    }
}