package tn.esprit.autoloc.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgence;

    @NotBlank
    private String nom;
    private String ville;
    private String adresse;
    private String telephone;

    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY)
    @JsonManagedReference("agence-employes")
    private List<Employe> employes = new ArrayList<>();

    /**
     * Nombre d'employés de l'agence (atelier 9, étape 11) — absent de l'UML, ajouté par l'atelier.
     * Colonne persistée, maintenue par le service lors de la mise à jour avec employés.
     */
    private int effectif;

    /* Atelier 9, étape 10 — symétrisation : on modifie TOUJOURS les deux côtés de la relation,
     * le côté propriétaire (employe.agence) est celui qui persiste. */
    public void addEmploye(Employe employe) {
        employes.add(employe);
        employe.setAgence(this);
    }

    public void removeEmploye(Employe employe) {
        employes.remove(employe);
        employe.setAgence(null);
    }
}
