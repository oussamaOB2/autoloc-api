package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.calcul.OffreTarifStrategy;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.util.Map;

@Service
public class VehiculeServiceImpl extends GenericServiceImpl<Vehicule, Long> implements IVehiculeService {

    /** Stratégies injectées par Spring, clés = noms des beans = constantes de l'enum (etape 16). */
    private final Map<String, OffreTarifStrategy> strategies;
    // Repositories (pas de service) : AgenceServiceImpl injecte IVehiculeService pour simuler,
    // injecter IAgenceService ici créerait un cycle de dépendances -> le contexte ne démarrerait pas.
    private final IAgenceRepository agenceRepository;
    private final IEquipementRepository equipementRepository;

    public VehiculeServiceImpl(IVehiculeRepository repository,
                               IAgenceRepository agenceRepository,
                               IEquipementRepository equipementRepository,
                               Map<String, OffreTarifStrategy> strategies) {
        super(repository);
        this.agenceRepository = agenceRepository;
        this.equipementRepository = equipementRepository;
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

    /* -------------------------------------- atelier 9, partie I : affectation/désaffectation */

    @Override
    @Transactional
    public void unassignAgence(Long idVehicule) {
        Vehicule vehicule = findById(idVehicule);   // 404 si absent
        // Côté propriétaire (ManyToMany/ManyToOne sans mappedBy = vehicule.agence) : dirty checking
        // dans la transaction suffit, aucun save explicite.
        vehicule.setAgence(null);
    }

    @Override
    @Transactional
    public Vehicule assignAgence(Long idVehicule, Long idAgence) {
        Vehicule vehicule = findById(idVehicule);   // 404 si absent
        Agence agence = agenceRepository.findById(idAgence)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", String.valueOf(idAgence)));
        vehicule.setAgence(agence);                 // seul le côté propriétaire persiste
        return vehicule;
    }

    @Override
    @Transactional
    public Vehicule assignEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = findById(idVehicule);   // 404 si absent
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", String.valueOf(idEquipement)));
        // Owner = Vehicule (@JoinTable vehicule_equipement) : c'est vehicule.getEquipements() qui
        // écrit la ligne de jointure. Le côté inverse est symétrisé en mémoire seulement.
        // contains() avant add() : clé primaire composée, un doublon échouerait en base.
        if (!vehicule.getEquipements().contains(equipement)) {
            vehicule.getEquipements().add(equipement);
            equipement.getVehicules().add(vehicule);
        }
        return vehicule;
    }
}
