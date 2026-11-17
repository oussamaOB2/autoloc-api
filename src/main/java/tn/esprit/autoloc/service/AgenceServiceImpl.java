package tn.esprit.autoloc.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.io.RapportFactory;
import tn.esprit.autoloc.repository.IAgenceRepository;

@Service
@Primary
public class AgenceServiceImpl extends GenericServiceImpl<Agence, Long> implements IAgenceService {

    private final IVehiculeService vehiculeService;                  // 1. NEW: the field
    private final RapportFactory rapportFactory;                     // SRP (etape 11) : la construction
                                                                     // du rapport n'est plus ici
    public AgenceServiceImpl(IAgenceRepository repository,
                             IVehiculeService vehiculeService,       // 2. NEW: the parameter
                             RapportFactory rapportFactory) {
        super(repository);
        this.vehiculeService = vehiculeService;                      // 3. NEW: the assignment
        this.rapportFactory = rapportFactory;
    }

    @Override
    protected String getEntityName() {
        return "Agence";
    }

    @Override
    protected Long getId(Agence entity) {
        return entity.getIdAgence();
    }

    @Override
    protected void setFields(Agence source, Agence target) {
        target.setNom(source.getNom());
        target.setVille(source.getVille());
        target.setAdresse(source.getAdresse());
        target.setTelephone(source.getTelephone());
    }

    @Override
    public boolean check(Agence agence, boolean isNew) {
        return agence != null && Utils.isNotEmpty(agence.getNom());
    }

    @Override
    public boolean hasVehicules(Long id) {
        Agence agence = findById(id);
        return agence.getVehicules() != null && !agence.getVehicules().isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public String simuler(Long idAgence, int jours, VehiculeOffre offre) {
        if (jours <= 0 || offre == null) {
            throw new IllegalArgumentException("Nombre de jours positif et offre obligatoires");
        }
        Agence agence = findById(idAgence);

        double total = 0;
        for (Vehicule v : agence.getVehicules()) {
            total += vehiculeService.calculTarif(v, jours, offre);
        }
        // Fabrique (etape 14) : type choisi ici, pas en parametre — signature simuler inchangée.
        // « CONSOLE » imprime l'en-tête sur la console et retourne le texte avec le total.
        return rapportFactory.getRapport("CONSOLE").construire(agence, total);
    }
}