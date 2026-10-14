package tn.esprit.autoloc.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.exception.ResourceNotFoundException;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractCrudService<T> implements ICrudService<T> {

    private final JpaRepository<T, Long> repository;
    private final String entityName;
    private final Function<T, Long> idExtractor;

    protected AbstractCrudService(JpaRepository<T, Long> repository,
                                  String entityName,
                                  Function<T, Long> idExtractor) {
        this.repository = repository;
        this.entityName = entityName;
        this.idExtractor = idExtractor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> retrieveAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public T retrieve(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName, String.valueOf(id)));
    }

    @Override
    @Transactional
    public T add(T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public T modify(T entity) {
        Long id = idExtractor.apply(entity);
        if (id == null || !repository.existsById(id)) {
            throw new ResourceNotFoundException(entityName, String.valueOf(id));
        }
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(entityName, String.valueOf(id));
        }
        repository.deleteById(id);
    }
}
