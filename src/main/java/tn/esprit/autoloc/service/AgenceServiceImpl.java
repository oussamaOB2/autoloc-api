package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.repository.IAgenceRepository;

@Service
public class AgenceServiceImpl extends AbstractCrudService<Agence> implements IAgenceService {

    public AgenceServiceImpl(IAgenceRepository repository) {
        super(repository, "Agence", Agence::getIdAgence);
    }

    @Override
    public Agence add(Agence agence) {
        if (!Utils.isNotEmpty(agence.getNom())) {
            throw new IllegalArgumentException("Le nom de l'agence est obligatoire");
        }
        return super.add(agence);
    }
}
