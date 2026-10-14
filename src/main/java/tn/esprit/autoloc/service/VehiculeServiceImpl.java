package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@Service
public class VehiculeServiceImpl extends AbstractCrudService<Vehicule> implements IVehiculeService {

    public VehiculeServiceImpl(IVehiculeRepository repository) {
        super(repository, "Vehicule", Vehicule::getIdVehicule);
    }
}
