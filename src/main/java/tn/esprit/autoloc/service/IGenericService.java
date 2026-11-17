package tn.esprit.autoloc.service;

public interface IGenericService<T, ID>
        extends IReadService<T, ID>, IWriteService<T, ID>, IDeleteService<ID> {
    boolean check(T entity, boolean isNew);
}