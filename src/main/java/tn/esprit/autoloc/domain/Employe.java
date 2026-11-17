package tn.esprit.autoloc.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "employe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmploye;

    @NotBlank
    private String nom;
    private String prenom;

    @Enumerated(EnumType.STRING)
    private RoleEmploye role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id")
    // Atelier 9, étape 13 : côté arrière de la référence "agence-employes" — non sérialisé
    // (évite le cycle Agence.employes <-> Employe.agence), mais rempli automatiquement
    // quand l'Agence parente est déserialisée (composition : l'enfant est créé dans le JSON du parent).
    @JsonBackReference("agence-employes")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Agence agence;
}
