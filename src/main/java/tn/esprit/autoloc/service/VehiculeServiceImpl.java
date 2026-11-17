package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.calcul.VehiculeOffre;

@Service
public class VehiculeServiceImpl extends GenericServiceImpl<Vehicule, Long> implements IVehiculeService {

    public VehiculeServiceImpl(IVehiculeRepository repository) {
        super(repository);
    }

    @Override
    protected String getEntityName() {
        return "Vehicule";
    }

    @Override
    protected Long getId(Vehicule entity) {
        return entity.getIdVehicule();
    }

    @Override
    protected void setFields(Vehicule source, Vehicule target) {
        target.setImmatriculation(source.getImmatriculation());
        target.setMarque(source.getMarque());
        target.setModele(source.getModele());
        target.setCategorie(source.getCategorie());
        target.setTarifJournalier(source.getTarifJournalier());
        target.setStatut(source.getStatut());
        target.setAgence(source.getAgence());
    }
    @Override
    public double calculTarif(Vehicule vehicule, int jours, VehiculeOffre offre) {
        double tarifJour = vehicule.getTarifJournalier().doubleValue();
        double total;
        switch (offre) {
            case WEEKEND -> total = tarifJour * 0.80 * jours;       // tarif journalier -20 %
            case LONGUE_DUREE -> {
                total = tarifJour * 0.75 * jours;                   // tarif journalier -25 %
                if (total > 2000) {
                    total = total * 0.95;                           // -5 % si le total dépasse 2000
                }
            }
            default -> total = tarifJour * jours;                   // STANDARD
        }
        return total;
    }
}
