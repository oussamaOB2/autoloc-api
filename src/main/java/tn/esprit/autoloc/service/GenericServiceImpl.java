package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.exception.ResourceNotFoundException;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericServiceImpl<T, ID> implements IGenericService<T, ID> {

    protected final JpaRepository<T, ID> repository;

    // Q15: Méthodes abstraites exigées
    protected abstract String getEntityName();
    protected abstract ID getId(T entity);
    protected abstract void setFields(T source, T target);

    @Override
    public boolean check(T entity, boolean isNew) {
        return entity != null;
    }

    @Override
    @Transactional
    public T create(T entity) {
        if (getId(entity) != null) {
            throw new IllegalArgumentException("L'identifiant doit être null lors de la création.");
        }
        if (!check(entity, true)) {
            throw new IllegalArgumentException("La vérification de l'entité a échoué.");
        }
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    @Override
    public T findById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), String.valueOf(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Transactional
    @Override
    public T update(ID id, T entity) {
        T existing = findById(id);
        if (!check(entity, false)) {
            throw new IllegalArgumentException("La vérification de l'entité a échoué.");
        }
        setFields(entity, existing);
        return repository.save(existing);
    }

    @Transactional
    @Override
    public void deleteById(ID id) {
        T existing = findById(id); // ResourceNotFoundException -> 404 when the row is missing
        repository.delete(existing);
        repository.flush();
    }
}