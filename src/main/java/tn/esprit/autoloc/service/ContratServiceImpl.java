package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.repository.IContratRepository;

@Service
public class ContratServiceImpl extends AbstractCrudService<Contrat> implements IContratService {

    public ContratServiceImpl(IContratRepository repository) {
        super(repository, "Contrat", Contrat::getIdContrat);
    }
}
