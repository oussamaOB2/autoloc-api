package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.service.ICrudService;

import java.util.List;

public abstract class AbstractCrudController<T> {

    private final ICrudService<T> service;

    protected AbstractCrudController(ICrudService<T> service) {
        this.service = service;
    }

    @Operation(summary = "Lister toutes les ressources")
    @GetMapping
    public List<T> getAll() {
        return service.retrieveAll();
    }

    @Operation(summary = "Obtenir une ressource par son identifiant")
    @ApiResponse(responseCode = "200", description = "Ressource trouvée")
    @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    @GetMapping("/{id}")
    public T getById(@PathVariable("id") Long id) {
        return service.retrieve(id);
    }

    @Operation(summary = "Créer une ressource")
    @ApiResponse(responseCode = "201", description = "Ressource créée")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T create(@Valid @RequestBody T body) {
        return service.add(body);
    }

    @Operation(summary = "Modifier une ressource (l'identifiant est obligatoire dans le corps)")
    @ApiResponse(responseCode = "200", description = "Ressource modifiée")
    @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    @PutMapping
    public T update(@Valid @RequestBody T body) {
        return service.modify(body);
    }

    @Operation(summary = "Supprimer une ressource")
    @ApiResponse(responseCode = "204", description = "Ressource supprimée")
    @ApiResponse(responseCode = "404", description = "Ressource introuvable")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        service.remove(id);
    }
}
