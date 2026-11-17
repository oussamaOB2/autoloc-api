# Chapitre 9 — B0 : propriétaires réels des relations (lu dans le code, pas supposé)

Source : `src/main/java/tn/esprit/autoloc/domain/*.java` (état de la branche `ateliers-8-9`).

| Relation | Côté OWNER (tient la clé physique) | Côté inverse (mappedBy) | Cascade | Fetch | Annotations JSON |
|---|---|---|---|---|---|
| **Vehicule ↔ Agence** | **Vehicule** — `@ManyToOne @JoinColumn(name = "agence_id")` (pas de mappedBy) | `Agence.vehicules` — `@OneToMany(mappedBy = "agence")` | aucun (ni REMOVE ni PERSIST) | LAZY (les deux côtés) | `Vehicule.agence` : `@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})` → **sérialisé** ; `Agence.vehicules` : `@JsonIgnore` |
| **Agence ↔ Employe** | **Employe** — `@ManyToOne @JoinColumn(name = "agence_id")` | `Agence.employes` — `@OneToMany(mappedBy = "agence")` | aucun | LAZY | `Employe.agence` : `@JsonIgnoreProperties(...)` → sérialisé ; `Agence.employes` : `@JsonIgnore` → **à changer (étape 13 : @JsonManagedReference / @JsonBackReference)** |
| **Vehicule ↔ Equipement** | **Vehicule** — `@ManyToMany @JoinTable(name = "vehicule_equipement", joinColumns = vehicule_id, inverseJoinColumns = equipement_id)` | `Equipement.vehicules` — `@ManyToMany(mappedBy = "equipements")` | aucun (pas de cascade sur le lien) | LAZY | les deux côtés `@JsonIgnore` (pas de cycle, pas de GET dédié) |
| **Reservation → Vehicule** | **Reservation** — `@ManyToOne @JoinColumn(name = "vehicule_id")` | aucun côté inverse : `Vehicule` n'a **pas** de collection `reservations` (relation unidirectionnelle) | aucun | LAZY | (Reservation côté véhicule non exposé) |
| **Maintenance → Vehicule** | **Maintenance** — `@ManyToOne @JoinColumn(name = "vehicule_id")` | aucun côté inverse dans `Vehicule` | **`cascade = CascadeType.PERSIST`** (unidirectionnel, PERSIST seulement — pas de REMOVE) | LAZY | idem |

## Conséquences pour les étapes 6 à 9 (ce que le code implique)

1. **Étapes 6-7 (assigner un équipement)** : le propriétaire est **Vehicule**, donc la modification
   qui persiste est `vehicule.getEquipements().add(equipement)` (côté owning de la join table).
   Le côté `equipement.getVehicules()` est inversé : on le met à jour en mémoire pour la symétrie,
   il ne provoque aucun INSERT dans la join table.
2. **Étape 8 (supprimer un véhicule lié)** : aucune cascade REMOVE nulle part → les lignes de
   `vehicule_equipement` (et `reservation.vehicule_id`, `maintenance.vehicule_id`) référencent
   toujours le véhicule → violation de clé étrangère attendue → **409** avec notre handler.
   Le propriétaire gère la mécanique de persistance, mais sans `cascade = REMOVE` Hibernate ne
   supprime pas les lignes enfants : c'est le SGBD qui refuse.
3. **Étape 9 (supprimer un équipement lié)** : `equipement_id` est une clé étrangère de la même
   join table → le SGBD devrait refuser aussi tant que le lien existe. À observer réellement et
   à rapporter tel quel (le support annonce « pas de problème »).
4. **Véhicule avec réservations/maintenances** : mêmes FK sans REMOVE → refus attendu aussi
   (à confirmer en réel ; `Maintenance` n'a que `PERSIST`, ce qui ne protège pas le DELETE).
