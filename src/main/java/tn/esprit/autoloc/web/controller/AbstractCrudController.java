package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.service.IGenericService;

import java.util.List;

public abstract class AbstractCrudController<T, ID> {

    private final IGenericService<T, ID> service;

    protected AbstractCrudController(IGenericService<T, ID> service) {
        this.service = service;
    }

    @Operation(summary = "Lister toutes les ressources")
    @GetMapping
    public List<T> findAll() {
        return service.findAll();
    }

    @Operation(summary = "Obtenir une ressource par son identifiant")
    @GetMapping("/{id}")
    public T getById(@PathVariable("id") ID id) {
        return service.findById(id);
    }

    @Operation(summary = "Créer une ressource")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T create(@Valid @RequestBody T body) {
        return service.create(body);
    }

    @Operation(summary = "Mettre à jour une ressource")
    @PutMapping("/{id}")
    public T update(@PathVariable("id") ID id, @Valid @RequestBody T body) {
        return service.update(id, body);
    }

    @Operation(summary = "Supprimer une ressource")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") ID id) {
        service.deleteById(id);
    }
}