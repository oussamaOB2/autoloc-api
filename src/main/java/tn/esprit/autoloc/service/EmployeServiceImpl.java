package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.repository.IEmployeRepository;

@Service
public class EmployeServiceImpl extends AbstractCrudService<Employe> implements ICrudService.IEmployeService {

    public EmployeServiceImpl(IEmployeRepository repository) {
        super(repository, "Employe", Employe::getIdEmploye);
    }
}
