package tn.esprit.autoloc.service;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.Utils;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.repository.IEmployeRepository;

@Service
public class EmployeServiceImpl extends GenericServiceImpl<Employe, Long> implements IEmployeService {

    public EmployeServiceImpl(IEmployeRepository repository) {
        super(repository);
    }

    @Override
    protected String getEntityName() {
        return "Employe";
    }

    @Override
    protected Long getId(Employe entity) {
        return entity.getIdEmploye();
    }

    @Override
    protected void setFields(Employe source, Employe target) {
        target.setNom(source.getNom());
        target.setPrenom(source.getPrenom());
        target.setRole(source.getRole());
        target.setAgence(source.getAgence());
    }

    /**
     * Atelier 9, étape 15 : le nom est obligatoire — c'est ce check qui rejette un employé
     * invalide À L'INTÉRIEUR du service (après le premier employé déjà inséré), ce qui rend
     * l'absence de transaction visible. Même motif que {@code AgenceServiceImpl.check}.
     */
    @Override
    public boolean check(Employe employe, boolean isNew) {
        return employe != null && Utils.isNotEmpty(employe.getNom());
    }
}
