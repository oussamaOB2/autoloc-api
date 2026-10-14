package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Employe;

import java.util.List;

public interface ICrudService<T> {

    List<T> retrieveAll();

    T retrieve(Long id);

    T add(T entity);

    T modify(T entity);

    void remove(Long id);

    interface IEmployeService extends ICrudService<Employe> {
    }
}
