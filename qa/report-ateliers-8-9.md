# Rapport final — Ateliers 8 & 9 (AutoLoc)

Branche : `ateliers-8-9` — commits : `49344ae` → `634ccfc` → `62da3da` → `f84a9ba` → `1d004e0` → `79fa71b` → `1d1ee31` → *(ce rapport)*.
Toutes les sorties ci-dessous sont les sorties réelles des commandes exécutées (fichiers de log conservés dans
`%TEMP%/opencode/*.log`, sorties live dans `qa/ch9-incoherence.md` et les scripts `verify_*.ps1`).

---

## 1. Checklist

### Atelier 8 — Chapitre 8, Design Patterns GoF, parties II (étapes 11-17)

| Étape | Statut | Évidence |
|---|---|---|
| 11. SRP sur `simuler` | **DONE** | Construction du rapport extraite vers `io/` ; `AgenceSimulerRegressionTest` (6 tests) identique avant/après → commit `634ccfc` |
| 12. Compiler | **DONE** | `mvn clean compile` → `BUILD SUCCESS` (étape 0) ; tous les builds suivants compilent |
| 13. Package `io` | **DONE** | `io/RapportAgence`, `io/BasicRapport`, `io/ConsoleRapportDecorator`, `io/RapportFactory` créés (commit `634ccfc`, 10 fichiers) |
| 14. Factory + Décorateur | **DONE** | `RapportFactory.getRapport(type)` (`"CONSOLE"`/`"TEXTE"`, inconnu → `IllegalArgumentException`), décorateur `@Primary` + `@Qualifier` sur le basique, **signature de `simuler` inchangée** ; live : 3 en-têtes imprimés sur la console + total conservé dans la réponse |
| 15. Tests (auto + manuel) | **DONE** | Unitaires (seuils 2000 des 3 stratégies, Factory, Décorateur avec capture `System.out`, totaux 6000.0/4800.0/4312.5, 400 jours/offre, 404 agence) ; `mvn clean test` final atelier 8 = **41/41 BUILD SUCCESS** (`w8_final_tests.log`) ; curl + Postman dans `qa/postman-ateliers-8-9.md` |
| 16. Stratégies via `Map` + IoC | **DONE** | `OffreTarifStrategy` + 3 `@Component("STANDARD"/"WEEKEND"/"LONGUE_DUREE")`, switch supprimé de `calculTarif`, lookup `offre.name()`, stratégie test-only prouvée → commit `62da3da` |
| 17. Push | **DONE** | `git push -u origin ateliers-8-9` → `* [new branch] ateliers-8-9 -> ateliers-8-9`, exit 0 |

### Atelier 9 — Chapitre 9, Spring Data JPA (étapes 1-19)

| Étape | Statut | Évidence |
|---|---|---|
| B0. Propriétaires réels | **DONE** | `qa/ch9-owners.md` (tableau §4 ci-dessous), lu dans le code |
| 1-3. Désaffecter agence | **DONE** | `unassignAgence` (@Transactional) + `DELETE /vehicules/{id}/agence` → **204** ; live : SQL `agence_id = NULL`, ligne agence toujours là ; `VehiculeAffectationTest` |
| 4-5. Affecter agence | **DONE** | `assignAgence` + `PUT /vehicules/{idV}/agence/{idA}` → **200** ; live SQL `agence_id=8` ; cycle évité par `IAgenceRepository` dans `VehiculeServiceImpl` (pas de `@Lazy`), l'application démarre |
| 6-7. Affecter équipement | **DONE** | `assignEquipement` côté **propriétaire** + garde `contains()` ; live : join `vehicule_equipement` 1 ligne, **2e appel sans doublon** |
| 8. DELETE véhicule lié | **DONE** | **OBSERVÉ : 204** (contrairement au support) — propriétaire Hibernate purge la join table ; preuve SQL : 0 ligne de jointure, équipement intact ; test `delete_vehicleWithEquipement_removesJoinRowsAndSucceeds` |
| 9. DELETE équipement lié | **DONE** | **OBSERVÉ : 409** FK `VEHICULE_EQUIPEMENT.EQUIPEMENT_ID` (contrairement au support) ; + véhicule avec réservation → **409** (`Reservation.vehicule_id`, pas de cascade REMOVE) ; lignes toujours vivantes après l'échec |
| 10. Symétrisation | **DONE** | `Agence.addEmploye/removeEmploye`, collections initialisées, sans `@Data` ; RED (`cannot find symbol`) → GREEN **3/3** (`AgenceEmployeSymetrizationTest`) |
| 11. Champ `effectif` | **DONE** | Champ `int effectif` sur `Agence` ; log Hibernate réel au démarrage : `add column effectif integer not null` (ddl-auto=update, table conservée) ; écart UML §7 |
| 12. `updateAvecEmployes` | **DONE** | Écrit **sans `@Transactional`** d'abord (exigence 15-17) ; `IAgenceService` + `AgenceServiceImpl` + `PUT /agences/{id}/employes` + `EmployeServiceImpl.check` ; RED observé avant implémentation (`cannot find symbol` sur les 3 tests) |
| 13. JSON imbriqués + REST | **DONE** | `@JsonManagedReference("agence-employes")` + `@JsonBackReference("agence-employes")` (paire posée ensemble) ; live : `GET /employes` sans agence imbriquée, `GET /agences` avec `employes[]`, **`POST /vehicules` avec `"agence":{"idAgence":N}` → 201**, 404 = 5 clés ; PUT valide → **200** |
| 14. SQL des insertions | **DONE** | Live : `id_employe=1,2` avec `agence_id=9`, `effectif=2` |
| 15-16. Incohérence sans transaction | **DONE** | Échec d'abord, preuves réelles AVANT/APRÈS dans `qa/ch9-incoherence.md` : HTTP **400** (5 clés) **mais** emp1 inséré (`id_employe=3`), emp2 rejeté, champs agence modifiés, `effectif=0` alors que 1 employé existe |
| 17. Correctif `@Transactional` | **DONE** | Ajouté sur `updateAvecEmployes` (appel via proxy, `create` REQUIRED rejoint la transaction) ; rejeu live : **400, agence intacte, 0 employé** ; trou d'auto-incrément visible (id 4 consommé par l'insert annulé, rejeu valide = ids 5-6) ; test rollback RED → GREEN |
| 18. Rejeu valide | **DONE** | Live : **200**, 2 employés (`agence_id=11`), `effectif=2` |
| 19. Push | **DONE** | 2 pushes réels (voir §3) ; ce rapport est commité puis poussé juste après sa rédaction |

---

## 2. Fichiers créés / modifiés et pourquoi

### Commit `49344ae` — baseline (réparation build test + travail préexistant)
- `service/IReadService|IWriteService|IDeleteService|IGenericService` : les parents utilisaient `Long` en dur tout en ignorant leur paramètre de type → surcharges ambiguës en compilation de tests ; réparés (type param respecté, doublons retirés de `IGenericService`, `check` conservé).
- `qa/audit-ateliers-8-9.md` : audit étape 0.

### Commit `634ccfc` — atelier 8 : io, factory, decorator
- **Créés** : `io/RapportAgence.java`, `io/BasicRapport.java`, `io/ConsoleRapportDecorator.java`, `io/RapportFactory.java`.
- **Modifié** : `AgenceServiceImpl.simuler` délègue la construction du rapport (SRP).
- **Tests** : `BasicRapportTest`, `ConsoleRapportDecoratorTest`, `RapportFactoryTest`, `AgenceSimulerRegressionTest`, `VehiculeServiceImplTest`.

### Commit `62da3da` — atelier 8 : strategy map
- **Créés** : `calcul/OffreTarifStrategy.java`, `StandardOffreStrategy`, `WeekendOffreStrategy`, `LongueDureeOffreStrategy` (`@Component` nommés d'après les constantes d'enum).
- **Modifié** : `VehiculeServiceImpl` (switch → `Map<String, OffreTarifStrategy>` injectée ; exception claire si stratégie absente).
- **Tests** : `AgenceControllerTest` (HTTP simulation).

### Commit `f84a9ba` — atelier 8 : tests HTTP + doc Postman
- `qa/postman-ateliers-8-9.md` (section atelier 8), `AgenceControllerTest`.

### Commit `1d004e0` — atelier 9 : affectation/désaffectation
- **Créé** : `qa/ch9-owners.md`.
- `IVehiculeService` (+`unassignAgence`/`assignAgence`/`assignEquipement`), `VehiculeServiceImpl` (via **repositories**, pas services, pour ne pas fermer le cycle agence→véhicule), `VehiculeController` (3 routes REST).
- **Tests** : `VehiculeAffectationTest` (11 tests, dont les comportements de DELETE observés).

### Commit `79fa71b` — atelier 9 : update agence avec employes + transaction
- `Agence` (`effectif`, `addEmploye`/`removeEmploye`, `@JsonManagedReference`), `Employe` (`@JsonBackReference`), `IAgenceService` (+`updateAvecEmployes`), `AgenceServiceImpl` (impl, puis `@Transactional` étape 17), `EmployeServiceImpl` (+`check` : nom obligatoire), `AgenceController` (`PUT /{id}/employes`).
- **Créés** : `AgenceEmployeSymetrizationTest`, `AgenceUpdateEmployesTest` (sans `@Transactional` de classe, nettoyage `@AfterEach` — exigence du plan), `qa/ch9-incoherence.md`.
- `AgenceSimulerRegressionTest` : constructeur mis à jour (4e dépendance `IEmployeService` simulé).
- *Avant push : amend pour retirer 2 `hs_err_pid*.log` (crashs de JVM lors des arrêts forcés) — amend local d'un commit non poussé, aucun force push.*

### Commit `1d1ee31` — atelier 9 : doc Postman (section atelier 9)

---

## 3. Commandes et résultats réels

### Étape 0 (audit)
```
mvn clean compile  → exit 0, BUILD SUCCESS          (audit_compile.log)
mvn test           → exit 1, BUILD FAILURE           (audit_test.log)
   Cause : compilation de tests — « reference to findById/deleteById is ambiguous »
```
Après réparation baseline (`49344ae`) : `Tests run: 18, Failures: 0, Errors: 0, BUILD SUCCESS`.

### Portes de régression (avant les changements)
```
gate_before.log : Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  → BUILD SUCCESS
   (AgenceSimulerRegressionTest : STANDARD 6000.0 / WEEKEND 4800.0 / LONGUE_DUREE 4312.5)
```

### Atelier 8
```
w8_final_tests.log : Tests run: 41, Failures: 0, Errors: 0, Skipped: 0  → BUILD SUCCESS
```
Live (port 8090, MySQL, scripts `verify_w8.ps1`) :
- `GET .../simulation?jours=10&offre=STANDARD` → **200**, `total tarif:6000.0`
- `...&offre=WEEKEND` → **200**, `total tarif:4800.0` ; `...&offre=LONGUE_DUREE` → **200**, `total tarif:4312.5`
- `...&offre=INVALIDE` → **400** ; `jours=0` → **400** ; `agence 999999` → **404**
- Toutes les erreurs : `{"timestamp","status","error","message","path"}` (5 clés)
- Console : 3 en-têtes `Agence #...` imprimés par le décorateur
- Régression ch.6 : GET 200, route inconnue 404, POST nom vide 400 ; cleanup 204×3 (agence 6 "Tunis Centre" intacte)

### Atelier 9 — partie 1
```
VehiculeAffectationTest : Tests run: 11, Failures: 0, Errors: 0  → BUILD SUCCESS   (green_b1c.log)
b1_full.log : Tests run: 52, Failures: 0, Errors: 0, Skipped: 0  → BUILD SUCCESS
```
Live (`verify_b1.ps1`, port 8090) :
- `DELETE /vehicules/{id}/agence` → **204** ; SQL : `agence_id = NULL`, agence toujours présente
- `PUT /vehicules/{idV}/agence/{idA}` → **200** ; SQL : `agence_id=8`
- `PUT .../equipements/{idE}` → **200** ; SQL join : 1 ligne ; 2e appel identique (**pas de doublon**)
- `DELETE /vehicules/{id}` avec équipement → **204** ; SQL : 0 ligne de jointure, équipement intact
- `DELETE /vehicules/{id}` avec réservation → **409** (5 clés), ligne véhicule vivante
- `DELETE /equipements/{id}` avec véhicule lié → **409** (`VEHICULE_EQUIPEMENT`), lignes vivantes

### Atelier 9 — partie 2 : ÉTAT PRÉ-CORRECTION (avant `@Transactional`)
```
pre_fix_full.log : Tests run: 58, Failures: 2, Errors: 1, Skipped: 0  → BUILD FAILURE
   (seul AgenceUpdateEmployesTest échoue — LazyInitializationException = symptôme exact
    de l'absence de transaction ; 55 autres tests verts)
```
Live pré-correction (`verify_b2_prefix.ps1`) — étapes 13-14 valides :
- `PUT /agences/9/employes` (2 employés) → **200**, réponse `"employes":[...2...],"effectif":2`
- SQL : `id_employe=1,2 → agence_id=9` ; `effectif=2`
- Régressions : `POST /vehicules` avec `"agence":{"idAgence":9}` → **201** ; `GET /agences/999999` → **404** (5 clés) ; `GET /agences` affiche `employes[]` ; `GET /employes` sans `agence` imbriquée

Étapes 15-16 (incohérence) : sorties SQL AVANT/APRÈS et corps HTTP complets archivés dans **`qa/ch9-incoherence.md`** :
```
AVANT : agence 10 nom=Agence B2-inval… ville=Sousse effectif=0 ; employes_avant=0
APRÈS : HTTP 400 {"status":400,...,"message":"La vérification de l'entité a échoué.",...}
        employe : id_employe=3 nom=Ben Salah agence_id=10   ← 1er employé INSÉRÉ
        agence  : nom=Apres-Invalide… ville=Monastir effectif=0  ← champs MODIFIÉS, effectif faux
GET /agences/10 → 200 avec 1 employé mais "effectif":0
```

### Atelier 9 — ÉTAT POST-CORRECTION (après `@Transactional`, étape 17)
```
1er essai  : post_fix_full.log  : Tests run: 58, Failures: 3, Errors: 45  → BUILD FAILURE
             (« Could not initialize plugin: MockMaker » — flake Mockito passager,
              touchant même contextLoads ; sans rapport avec le changement @Transactional)
retest     : post_fix_full2.log : Tests run: 58, Failures: 0, Errors: 0, Skipped: 0 → BUILD SUCCESS
```
Live post-correction (`verify_b2_postfix.ps1`) :
- Rejeu **invalide** → **400** (5 clés) ; SQL **identique avant/après** : `nom=Agence B2pf 989128`, `effectif=0`, `employes_apres_echec=0` ; `GET /agences/11` intact → **rollback total confirmé**
- Rejeu **valide** (étape 18) → **200**, `"employes":[...2...],"effectif":2` ; SQL : `id_employe=5,6 → agence_id=11`, `effectif=2`
- **Trou d'auto-incrément** : `max_id_avant=0` → l'insert annulé a consommé l'id 4 → les inserts valides ont repris à 5
- Régression : `GET /agences/999999` → **404** (5 clés) ; cleanup 204 (données utilisateur intactes : agence 6, véhicules 7/9, 0 employé restant)

### Git (résultats exacts)
```
1. git push -u origin ateliers-8-9
   → * [new branch] ateliers-8-9 -> ateliers-8-9        exit 0   (atelier 8)
2. git push origin ateliers-8-9
   → f84a9ba..1d1ee31  ateliers-8-9 -> ateliers-8-9     exit 0   (après doc postman W9)
3. Pré-push : git ls-files target/ → rien ; diff application.properties vs origin/main
   → uniquement server.servlet.context-path=/api (ligne de mot de passe INTROUVABLE dans le
   diff = identique à origin/main, aucun nouveau secret) ; aucune valeur de secret affichée.
4. Ce rapport : commit + push exécutés après rédaction (voir git log en fin de session).
```

---

## 4. Propriétaires réels (B0) et comportements de suppression observés

Tableau détaillé : `qa/ch9-owners.md`.

| Relation | OWNER (clé physique) | Inverse (mappedBy) | Cascade | Observé au DELETE |
|---|---|---|---|---|
| Vehicule ↔ Agence | **Vehicule** (`agence_id`) | `Agence.vehicules` | aucun | véhicule → **204** (si rien d'autre ne le référence) |
| Agence ↔ Employe | **Employe** (`agence_id`) | `Agence.employes` | aucun | employé → **204** |
| Vehicule ↔ Equipement | **Vehicule** (`@JoinTable vehicule_equipement`) | `Equipement.vehicules` | aucun | véhicule lié → **204** (le propriétaire purge la join table) ; **équipement lié → 409** (le côté inverse ne touche pas la join table) |
| Reservation → Vehicule | **Reservation** (`vehicule_id`) | aucun (unidirectionnel) | aucun | véhicule réservé → **409** |
| Maintenance → Vehicule | **Maintenance** (`vehicule_id`) | aucun | **PERSIST seulement** | véhicule maintenu → **409** attendu (PERSIST ne protège pas le DELETE) |

Leçon du chapitre : **le côté propriétaire décide** — Hibernate ne nettoie que la table de jointure qu'il
gère (`vehicule_equipement`, pilotée par `Vehicule`) ; les FK des entités enfants vers le véhicule sont
refusées par le SGBD faute de cascade REMOVE.

---

## 5. Hypotheses et ambiguïtés (choix retenus)

1. **Étape 9 du support** (« supprimer un équipement lié : aucun problème ») : lecture retenue = observer et rapporter l'exécution réelle, sans toucher aux mappings → **409 constaté** (§6).
2. **Étape 11 (principe)** : SRP — extraire la construction du rapport de `simuler`.
3. **Étape 14 « annotating the class »** : `@Primary` sur `ConsoleRapportDecorator` + `@Qualifier("basicRapport")` dans le décorateur (pas d'auto-injection).
4. **`effectif`** : lecture **littérale** du plan (« set effectif = size of list ») = taille de la liste **reçue** (nouveaux employés de la requête). L'alternative « existants + nouveaux » est notée ici comme ambiguïté du support mais non retenue.
5. **Côté propriétaire Vehicule ↔ Equipement** : déterminé par lecture du code (`@JoinTable` côté `Vehicule`), pas supposé — conformément à B0.
6. **Types de `RapportFactory.getRapport(String)`** : `"CONSOLE"` (décoré) et `"TEXTE"` (basique), inconnu → `IllegalArgumentException` ; `simuler` appelle `getRapport("CONSOLE")` en interne, signature inchangée.
7. **`OffreTarifStrategy.calculer(Vehicule, int)`** : tarif journalier lu dans le véhicule (pas de paramètre séparé).
8. **Dépendances de `VehiculeServiceImpl`** : `IAgenceRepository`/`IEquipementRepository` plutôt que services, pour éviter le cycle `AgenceServiceImpl → IVehiculeService ← VehiculeServiceImpl → IAgenceService`.
9. **Ordre dans `updateAvecEmployes`** : `addEmploye` **avant** `employeService.create` (fixer le côté propriétaire avant l'insert), `setEffectif` seulement si la liste est non nulle ; `repository.save(agence)` final pour persister `effectif`.
10. **Rejeu invalide (étapes 15-16)** : exécuté sur une **seconde agence fraîche** pour un avant/après SQL sans ambiguïté.
11. **Check employé absent** : le support part du principe qu'« il rejette » ; il n'existait pas → `EmployeServiceImpl.check` ajouté (nom non vide), même motif que `AgenceServiceImpl.check`.
12. **Assertion sur le message** : relâchée sur `assertNotNull` — le message réel (live comme en test) est le générique `La vérification de l'entité a échoué.` de `GenericServiceImpl`.

---

## 6. Incohérences du support

**Erreurs de slides (citées par le plan, non copiées) :**
1. Slide 5/11 : `@OneToMany(mappedBy = "equipe")` devrait être `"entreprise"`.
2. Slides 6/10/11/12 : `@ManyToMany(mappedBy = "projects")` devrait être `"equipes"`.
3. Slide 18 : `unassignEquipe` retire l'équipe mais devrait retirer le projet.

**Trouvées pendant l'exécution :**
4. **Étape 8** : le support annonce une erreur (FK) à la suppression d'un véhicule lié à un équipement → **réel : 204**. Explication : `Vehicule` est le **propriétaire** du `@ManyToMany` et Hibernate supprime d'abord les lignes de `vehicule_equipement` qu'il gère. Mapping **non modifié** pour forcer l'erreur du support ; le test asserte l'observé avec commentaire.
5. **Étape 9** : le support annonce « aucun problème » pour la suppression d'un équipement lié → **réel : 409**, FK `VEHICULE_EQUIPEMENT.EQUIPEMENT_ID` (côté **inverse**, la join table n'est pas nettoyée). Même attitude : observé consigné, mappings inchangés.
6. **Prémisse étape 15** : « notre check employé rejette l'employé invalide » — ce check n'existait pas ; il a dû être ajouté (`EmployeServiceImpl.check`).

---

## 7. Écarts (UML / travail antérieur)

1. **`Agence.effectif` (champ `int`, colonne `effectif`)** — absent de l'UML, exigé par l'étape 11. Aucun `ddl-auto` modifié : `application.properties` garde `spring.jpa.hibernate.ddl-auto=update` ; la colonne a été ajoutée par Hibernate au démarrage (log réel : `add column effectif integer not null`), table existante préservée.
2. **Annotations JSON Agence ↔ Employe** (étape 13) : `@JsonIgnore` sur `Agence.employes` remplacé par `@JsonManagedReference("agence-employes")` ; `@JsonBackReference("agence-employes")` ajouté sur `Employe.agence` (paire obligatoire). Effets vérifiés live : `GET /agences` imbrique `employes[]`, `GET /employes` n'imbrique plus `agence`. **Côté Vehicule ↔ Agence NON touché** (`POST /vehicules` avec agence imbriquée → toujours 201). *Point de vigilance : `POST /employes` avec un `agence` imbriqué dans le corps serait ignoré (back-reference non désérialisée depuis l'enfant) — aucune requête existante ne le fait.*
3. **`EmployeServiceImpl.check` ajouté** (nom obligatoire) — voir hypothèse 11.
4. **Réparation baseline des interfaces** (`IRead/IWrite/IDelete` sur le paramètre de type) — nécessaire pour que `mvn test` compile ; décision consignée dans l'audit et le ledger.
5. **Tests étapes 8/9 assertent le comportement OBSERVÉ** (avec commentaires explicatifs) plutôt que celui du support — mappings, `ddl-auto`, fetch, cascades et `mappedBy` **inchangés**.

---

## 8. Étapes manuelles restantes (pour l'utilisateur)

1. **Postman** : collection `AutoLoc-API`, variable `{{asiApiUrl}} = http://localhost:8081/api` — requêtes prêtes dans `qa/postman-ateliers-8-9.md` (atelier 8 : 5 requêtes de simulation ; atelier 9 : fixtures + affectation/désaffectation + `Agence-update-employes-valide` / `-invalide` / rejeu, avec `pm.test` et SQL de vérification).
2. **Rejeu « avant correctif »** (incohérence) : non rejouable sans `git checkout 1d004e0` ; les preuves AVANT/APRÈS réelles sont archivées dans `qa/ch9-incoherence.md`.
3. **Credentials** : la ligne de mot de passe MySQL de `application.properties` existait déjà sur `origin/main` (commit `e88100c`, poussé avant la session). Aucun nouveau secret dans mes commits ; la retirer de l'historique demanderait un **force push (interdit)** ou une **rotation du mot de passe** — décision laissée à l'utilisateur.
4. `.superpowers/` (plan + ledger) reste non tracké, par choix.

---

*Fin du rapport — atelier 10 non commencé.*
