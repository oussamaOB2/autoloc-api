package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.repository.IEquipementRepository;

@Service
public class EquipementServiceImpl extends AbstractCrudService<Equipement> implements IEquipementService {

    public EquipementServiceImpl(IEquipementRepository repository) {
        super(repository, "Equipement", Equipement::getIdEquipement);
    }
}
