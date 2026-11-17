package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.calcul.OffreTarifStrategy;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.util.Map;

@Service
public class VehiculeServiceImpl extends GenericServiceImpl<Vehicule, Long> implements IVehiculeService {

    /** Stratégies injectées par Spring, clés = noms des beans = constantes de l'enum (etape 16). */
    private final Map<String, OffreTarifStrategy> strategies;

    public VehiculeServiceImpl(IVehiculeRepository repository,
                               Map<String, OffreTarifStrategy> strategies) {
        super(repository);
        this.strategies = strategies;
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
        if (offre == null) {
            throw new IllegalArgumentException("Offre obligatoire");
        }
        OffreTarifStrategy strategy = strategies.get(offre.name());
        if (strategy == null) {
            throw new IllegalArgumentException("Aucune stratégie de tarif pour l'offre " + offre.name());
        }
        return strategy.calculer(vehicule, jours);
    }
}
