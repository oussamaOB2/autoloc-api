# Audit — Ateliers 8 & 9 (état réel, effectué avant toute modification)

Date : 2026-11-17 · Branche : `main` @ `e88100c` + travail non committé (chapitres 5-6, atelier 8 étapes 1-10)

## 1. État git

- `git log --oneline -20` :
  - `e88100c` feat: complete implementation of AutoLoc domain, repository, service, controller, and Swagger layers
  - `88f61d0` feat(chapitre-2): ajouts des 8 entites supplementaires et enums selon le diagramme UML
  - `2d501f9` feat(chapitre-2): creation du projet et des 9 entites de base sans associations
- Branche courante : `main`. Remote configuré : `origin https://github.com/oussamaOB2/autoloc-api.git`.
- Travier non committé : migration chapitre 5 (`IGenericService`), chapitre 6 parties I-II (handler dans `web/`, context-path `/api`), atelier 8 étapes 1-10.

## 2. Fichiers par package

- `service/` : 14 interfaces (`IGenericService`, `IReadService`, `IWriteService`, `IDeleteService`, `IAgenceService`, `IClientService`, `IContratService`, `IEmployeService`, `IEquipementService`, `IMaintenanceService`, `IPaiementService`, `IReservationService`, `IVehiculeService`) + 9 impls (`AgenceServiceImpl`, `ClientServiceImpl`, `ContratServiceImpl`, `EmployeServiceImpl`, `EquipementServiceImpl`, `GenericServiceImpl`, `MaintenanceServiceImpl`, `PaiementServiceImpl`, `ReservationServiceImpl`, `VehiculeServiceImpl`).
- `web/` : `ErrorResponse.java`, `GlobalExceptionHandler.java`, `controller/` (10 contrôleurs + `AbstractCrudController` + `OpenApiConfig`), `dto/` vide.
- `calcul/` : `VehiculeOffre.java` (enum).
- `io/` : **n'existe pas encore** (étape 13 = TODO).
- `exception/` : `ResourceNotFoundException.java`.

## 3. Atelier 8 — étapes 1-10 : vérification pièce par pièce (pas d'hypothèse)

| Élément | Statut | Preuve |
|---|---|---|
| Découpage d'interfaces `IReadService` / `IWriteService` / `IDeleteService` / `IGenericService` | **PARTIEL** | Les 4 fichiers existent. `IGenericService` re-déclare `findById`/`update`/`deleteById` (déjà hérités de `IRead`/`IWrite`/`IDelete`) → **`mvn test` ne compile pas** (voir §4). |
| `IPaiementService` en lecture seule | **DONE** | `IPaiementService extends IReadService<Paiement, Long>` sans méthode d'écriture ; `PaiementController` n'a que `GET` et `GET /{id}`. |
| `calcul/VehiculeOffre` | **DONE** | `src/main/java/tn/esprit/autoloc/calcul/VehiculeOffre.java` existe (enum). |
| `calculTarif` | **DONE** | `VehiculeServiceImpl.calculTarif` : `WEEKEND` ×0.80, `LONGUE_DUREE` ×0.75 puis ×0.95 si total > 2000, sinon STANDARD ×1. Le `switch` est présent (à remplacer en étape 16). |
| `simuler` | **DONE** | `IAgenceService.simuler(Long, int, VehiculeOffre)` ; impl dans `AgenceServiceImpl` (en-tête + boucle véhicules + `total tarif:`), `@Transactional(readOnly = true)`. |
| `GET /agences/{id}/simulation` | **DONE** | `AgenceController` : `@GetMapping("/{id}/simulation")` produces `text/plain`, params `jours`, `offre`. |
| `io/` (étapes 13-14) | **TODO** | Répertoire inexistant. |
| Stratégies `Map<,>` (étape 16) | **TODO** | Le `switch` est toujours dans `calculTarif`. |
| Tests de simulation (étape 15) | **TODO** | Aucun test ne couvre `simuler` / `calculTarif` / la route `/simulation` (5 classes de tests seulement : application, Utils, Associations, AgenceServiceImpl, AgenceController). |
| Étape 17 (push) | **TODO** | Dernier push : non vérifié ici ; remote configuré. |

## 4. Résultats réels des commandes (baseline)

- `mvn clean compile` → **exit 0, BUILD SUCCESS**.
- `mvn test` → **exit 1, BUILD FAILURE** en compilation de tests :
  - `AgenceControllerTest.java:[39,21] reference to findById is ambiguous`
  - `AgenceControllerTest.java:[85,24] reference to deleteById is ambiguous`
  - Cause : `IGenericService<T,ID>` redéfinit `findById(ID)`/`update(ID,T)`/`deleteById(ID)` alors que `IReadService`/`IWriteService`/`IDeleteService` déclarent `findById(Long)`/`update(Long,T)`/`deleteById(Long)`. Appelés via l'interface avec une valeur `long` littéral, les deux surcharges sont applicables → ambiguïté. (Appels sur la classe concrète ou avec une variable de type `ID` : pas d'ambiguïté, d'où un `mvn compile` vert.)
  - Aucun test n'a tourné : la baseline est **rouge**, à réparer avant toute étape de l'atelier 8.

## 5. Passes de régression (à respecter avant/après)

- Fixture : 1 agence, 2 véhicules (tarif 100 et 500), 10 jours → `simuler` : STANDARD **6000.0**, WEEKEND **4800.0**, LONGUE_DUREE **4312.5** (750 + 3750×0.95). Vérification manuelle via curl tant qu'aucun test automatisé n'existe.
- Comportement CRUD chapitre 6 (`/api/agences`, `/api/vehicules`) et clés d'erreur JSON (`timestamp`, `status`, `error`, `message`, `path`) : inchangé (tests `AgenceControllerTest` + vérification live en partie II du chapitre 6).
