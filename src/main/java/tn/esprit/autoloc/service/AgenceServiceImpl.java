package tn.esprit.autoloc.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.calcul.VehiculeOffre;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.io.RapportFactory;
import tn.esprit.autoloc.repository.IAgenceRepository;

@Service
@Primary
public class AgenceServiceImpl extends GenericServiceImpl<Agence, Long> implements IAgenceService {

    private final IVehiculeService vehiculeService;                  // 1. NEW: the field
    private final RapportFactory rapportFactory;                     // SRP (etape 11) : la construction
                                                                     // du rapport n'est plus ici
    private final IEmployeService employeService;                    // etape 12 : création des employés
                                                                     // (pas de cycle : EmployeServiceImpl
                                                                     // n'injecte qu'un repository)
    public AgenceServiceImpl(IAgenceRepository repository,
                             IVehiculeService vehiculeService,       // 2. NEW: the parameter
                             RapportFactory rapportFactory,
                             IEmployeService employeService) {
        super(repository);
        this.vehiculeService = vehiculeService;                      // 3. NEW: the assignment
        this.rapportFactory = rapportFactory;
        this.employeService = employeService;
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

    /**
     * Atelier 9, étapes 12-18 : mise à jour d'une agence avec ajout d'employés.
     * <p>
     * Étape 17 : {@code @Transactional} englobe la mise à jour, les insertions d'employés
     * ({@code create} en runtime REQUIRED rejoignent la transaction) et {@code setEffectif} —
     * la première exception (employé invalide) annule tout (rollback par défaut sur RuntimeException).
     * Appelé via le proxy Spring (pas d'auto-involution depuis la même classe).
     */
    @Override
    @Transactional
    public Agence updateAvecEmployes(Long idAgence, Agence source) {
        if (source == null) {
            throw new IllegalArgumentException("Agence source obligatoire");
        }
        // setFields ne copie que les scalaires (nom, ville, ...) : employes/effectif ignorés ici.
        Agence agence = update(idAgence, source);
        if (source.getEmployes() != null) {
            for (Employe employe : source.getEmployes()) {
                // symétrie (étape 10) : fixe employe.agence, le côté propriétaire, AVANT l'insert.
                agence.addEmploye(employe);
                employeService.create(employe);   // check de l'employé : rejet si nom absent
            }
            agence.setEffectif(source.getEmployes().size());
            repository.save(agence);
        }
        return agence;
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