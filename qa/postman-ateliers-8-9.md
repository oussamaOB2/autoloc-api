# Postman — Ateliers 8 & 9

Variable d'environnement : `{{asiApiUrl}}` = `http://localhost:8081/api` (environnement `local`).
Collection : `AutoLoc-API`.

> Pré-requis atelier 8 : une agence avec **2 véhicules** (tarifJournalier **100** et **500**).
> L'identifiant de l'agence est `{{agenceId}}` (posé par la requête `Ajout-sans-id` du chapitre 6,
> ou créez l'agence et les deux véhicules d'abord).

---

## Atelier 8 — Simulation (étape 15, manuel)

### Simulation-standard
- **GET** `{{asiApiUrl}}/agences/{{agenceId}}/simulation?jours=10&offre=STANDARD`
- Corps : aucun

Scripts → Post-response :
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
pm.test("Total STANDARD = 6000.0", function () {
    pm.expect(pm.response.text()).to.include("total tarif:6000.0");
});
pm.test("En-tête de l'agence présent", function () {
    pm.expect(pm.response.text()).to.include("Agence #" + pm.environment.get("agenceId"));
});
```

### Simulation-weekend
- **GET** `{{asiApiUrl}}/agences/{{agenceId}}/simulation?jours=10&offre=WEEKEND`
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
pm.test("Total WEEKEND = 4800.0", function () {
    pm.expect(pm.response.text()).to.include("total tarif:4800.0");
});
```

### Simulation-longue-duree
- **GET** `{{asiApiUrl}}/agences/{{agenceId}}/simulation?jours=10&offre=LONGUE_DUREE`
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
pm.test("Total LONGUE_DUREE = 4312.5", function () {
    pm.expect(pm.response.text()).to.include("total tarif:4312.5");
});
```

### Simulation-offre-invalide (étape 15 : invalide → 400)
- **GET** `{{asiApiUrl}}/agences/{{agenceId}}/simulation?jours=10&offre=INVALIDE`
```js
pm.test("Statut 400", function () { pm.response.to.have.status(400); });
pm.test("Format d erreur de l API", function () {
    var b = pm.response.json();
    pm.expect(b).to.have.property("timestamp");
    pm.expect(b.status).to.eql(400);
    pm.expect(b.error).to.eql("Bad Request");
    pm.expect(b.message).to.be.a("string");
    pm.expect(b.path).to.eql("/api/agences/" + pm.environment.get("agenceId") + "/simulation");
});
```

### Simulation-agence-inconnue (étape 15 : inconnue → 404)
- **GET** `{{asiApiUrl}}/agences/999999/simulation?jours=10&offre=STANDARD`
```js
pm.test("Statut 404", function () { pm.response.to.have.status(404); });
pm.test("Format d erreur de l API", function () {
    var b = pm.response.json();
    pm.expect(b.status).to.eql(404);
    pm.expect(b.error).to.eql("Not Found");
    pm.expect(b.path).to.eql("/api/agences/999999/simulation");
});
```

### Équivalents curl (atelier 8)
```bash
curl "http://localhost:8081/api/agences/{{agenceId}}/simulation?jours=10&offre=STANDARD"
# -> Agence #7 - Agence Simulation - Tél : 20000000
#    total tarif:6000.0

curl "http://localhost:8081/api/agences/{{agenceId}}/simulation?jours=10&offre=WEEKEND"
# -> total tarif:4800.0

curl "http://localhost:8081/api/agences/{{agenceId}}/simulation?jours=10&offre=LONGUE_DUREE"
# -> total tarif:4312.5

curl -i "http://localhost:8081/api/agences/{{agenceId}}/simulation?jours=10&offre=INVALIDE"
# -> HTTP/1.1 400  {"timestamp":...,"status":400,"error":"Bad Request",...}

curl -i "http://localhost:8081/api/agences/{{agenceId}}/simulation?jours=0&offre=STANDARD"
# -> HTTP/1.1 400  {"message":"Nombre de jours positif et offre obligatoires",...}

curl -i "http://localhost:8081/api/agences/999999/simulation?jours=10&offre=STANDARD"
# -> HTTP/1.1 404  {"message":"Agence introuvable avec l'id 999999",...}
```

### SQL — vérification de la fixture (atelier 8)
```sql
SELECT id_agence, nom FROM agence;
SELECT id_vehicule, immatriculation, tarif_journalier, agence_id
FROM vehicule WHERE agence_id = <idAgence>;   -- doit montrer 2 lignes : 100.00 et 500.00
```

> Côté serveur, chaque simulation imprime aussi l'en-tête sur la **console** (System.out) :
> c'est le Décorateur `ConsoleRapportDecorator` — visible dans la log d'IntelliJ.

---

## Atelier 9 — Affectation / Désaffectation

> Pré-requis : une agence `{{agenceIdA}}`, un véhicule `{{vehiculeId}}`, un équipement
> `{{equipementId}}` (les créations du chapitre 6 suffisent). Les identifiants peuvent être
> posés par les requêtes `Fixture-*` ci-dessous.

### Fixture-agence
- **POST** `{{asiApiUrl}}/agences`
```json
{ "nom": "Agence Postman W9", "ville": "Tunis", "adresse": "1 rue W9", "telephone": "71000000" }
```
```js
pm.test("Statut 201", function () { pm.response.to.have.status(201); });
pm.environment.set("agenceIdA", pm.response.json().idAgence);
```

### Fixture-vehicule
- **POST** `{{asiApiUrl}}/vehicules`
```json
{ "immatriculation": "TN-W9-001", "marque": "Peugeot", "modele": "208",
  "categorie": "CITADINE", "tarifJournalier": 90, "statut": "DISPONIBLE",
  "agence": { "idAgence": {{agenceIdA}}} }
```
```js
pm.test("Statut 201 (agence imbriquee : regression ch.6)", function () {
    pm.response.to.have.status(201);
});
pm.environment.set("vehiculeId", pm.response.json().idVehicule);
```

### Fixture-equipement
- **POST** `{{asiApiUrl}}/equipements`
```json
{ "libelle": "Equipement W9" }
```
```js
pm.test("Statut 201", function () { pm.response.to.have.status(201); });
pm.environment.set("equipementId", pm.response.json().idEquipement);
```

### Desaffecter-agence-vehicule (étape 3)
- **DELETE** `{{asiApiUrl}}/vehicules/{{vehiculeId}}/agence`
```js
pm.test("Statut 204", function () { pm.response.to.have.status(204); });
```
```sql
-- verification : l'agence est nulle, la ligne agence existe toujours
SELECT id_vehicule, agence_id FROM vehicule WHERE id_vehicule = {{vehiculeId}};  -- agence_id = NULL
SELECT id_agence, nom FROM agence WHERE id_agence = {{agenceIdA}};               -- 1 ligne
```

### Affecter-agence-vehicule (étape 5)
- **PUT** `{{asiApiUrl}}/vehicules/{{vehiculeId}}/agence/{{agenceIdA}}`
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
```
```sql
SELECT id_vehicule, agence_id FROM vehicule WHERE id_vehicule = {{vehiculeId}};  -- agence_id = {{agenceIdA}}
```

### Affecter-equipement-vehicule (étapes 6-7)
- **PUT** `{{asiApiUrl}}/vehicules/{{vehiculeId}}/equipements/{{equipementId}}`
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
```
> Relancer la même requête : **pas de doublon** (garde `contains()` côté service).
```sql
SELECT COUNT(*) FROM vehicule_equipement;   -- 1 pour ce couple, même après 2 appels
```

### Supprimer-vehicule-avec-equipement (étape 8 — OBSERVÉ contrairement au support)
- **DELETE** `{{asiApiUrl}}/vehicules/{{vehiculeId}}`
```js
// OBSERVÉ : 204, PAS d'erreur. Vehicule est le côté PROPRIÉTAIRE du @ManyToMany
// (@JoinTable vehicule_equipement) : Hibernate supprime les lignes de jointure
// qu'il gère AVANT la ligne vehicule. Le support annonçait un échec →
// voir « Incohérences du support » dans qa/report-ateliers-8-9.md.
pm.test("Statut 204 (comportement observé, mapping non modifié)", function () {
    pm.response.to.have.status(204);
});
```
```sql
SELECT COUNT(*) FROM vehicule_equipement;                 -- 0 (lignes de jointure nettoyées)
SELECT id_equipement FROM equipement WHERE id_equipement = {{equipementId}};  -- toujours là
```
> Variante **réellement refusée** (409) : réaffecter un véhicule à une **réservation** puis
> `DELETE /vehicules/{id}` → `Reservation.vehicule_id` FK sans cascade REMOVE.

### Supprimer-equipement (étape 9 — OBSERVÉ contrairement au support)
1. Recréer un véhicule, le lier à l'équipement, puis :
- **DELETE** `{{asiApiUrl}}/equipements/{{equipementId}}`
```js
// OBSERVÉ : 409 — Equipement est le côté INVERSE (mappedBy = "equipements") : la FK
// EQUIPEMENT_ID de la table de jointure vehicule_equipement refuse la suppression.
// Le support annonçait « aucun problème » → voir rapport.
pm.test("Statut 409 (comportement observé)", function () { pm.response.to.have.status(409); });
pm.test("Format d erreur de l API", function () {
    var b = pm.response.json();
    pm.expect(b).to.have.property("timestamp");
    pm.expect(b).to.have.property("status");
    pm.expect(b).to.have.property("error");
    pm.expect(b).to.have.property("message");
    pm.expect(b).to.have.property("path");
});
```

### Agence-update-employes-valide (étapes 13-14, 18)
- **PUT** `{{asiApiUrl}}/agences/{{agenceIdA}}/employes`
```json
{ "nom": "Apres-Valide", "ville": "Sfax", "adresse": "2 rue W9", "telephone": "74000000",
  "employes": [
    { "nom": "Ben Salah", "prenom": "Ali", "role": "AGENT" },
    { "nom": "Trabelsi", "prenom": "Sonia", "role": "MANAGER" }
  ] }
```
```js
pm.test("Statut 200", function () { pm.response.to.have.status(200); });
pm.test("2 employes imbriques + effectif = 2", function () {
    var b = pm.response.json();
    pm.expect(b.employes.length).to.eql(2);
    pm.expect(b.effectif).to.eql(2);
});
```
```sql
SELECT id_employe, nom, agence_id FROM employe WHERE agence_id = {{agenceIdA}};  -- 2 lignes
SELECT effectif FROM agence WHERE id_agence = {{agenceIdA}};                      -- 2
```

### Agence-update-employes-invalide (étapes 15-17)
- **PUT** `{{asiApiUrl}}/agences/{{agenceIdA}}/employes` — même corps mais le **2e employé
  sans `nom`** :
```json
{ "nom": "Apres-Invalide", "ville": "Monastir", "adresse": "4 rue W9", "telephone": "73500000",
  "employes": [
    { "nom": "Ben Salah", "prenom": "Ali", "role": "AGENT" },
    { "prenom": "SansNom", "role": "AGENT" }
  ] }
```
```js
pm.test("Statut 400 (mapped par GlobalExceptionHandler)", function () {
    pm.response.to.have.status(400);
});
pm.test("Format d erreur de l API", function () {
    var b = pm.response.json();
    pm.expect(b).to.have.property("timestamp");
    pm.expect(b.status).to.eql(400);
    pm.expect(b.error).to.eql("Bad Request");
    pm.expect(b.message).to.be.a("string");
    pm.expect(b.path).to.eql("/api/agences/" + pm.environment.get("agenceIdA") + "/employes");
});
```
```sql
-- APRES correctif (@Transactional) : aucune modification (rollback total)
SELECT COUNT(*) FROM employe WHERE agence_id = {{agenceIdA}};   -- inchangé
SELECT nom, effectif FROM agence WHERE id_agence = {{agenceIdA}};  -- inchangés
```

> **Avant correctif** (commit `1d004e0` et précédents), cette même requête produisait
> l'incohérence documentée : HTTP 400 **mais** 1er employé inséré, champs agence modifiés,
> `effectif` non mis à jour. Sorties SQL avant/après réelles capturées dans
> `qa/ch9-incoherence.md` — l'état « avant » n'est plus rejouable sans régression git
> (décision : le correctif est la finalité de l'atelier, étape 17).

### Rejeu-apres-rollback (étape 18)
Renvoyer **Agence-update-employes-valide** après l'échec :
```js
pm.test("Statut 200 apres le rollback", function () { pm.response.to.have.status(200); });
```
```sql
SELECT id_employe FROM employe WHERE agence_id = {{agenceIdA}} ORDER BY id_employe;
-- 2 lignes ; les ids sautent ceux consommés par l'insert annulé (trou d'auto-incrément)
```

### Équivalents curl (atelier 9)
```bash
curl -i -X DELETE "http://localhost:8081/api/vehicules/{{vehiculeId}}/agence"
# -> HTTP/1.1 204

curl -i -X PUT "http://localhost:8081/api/vehicules/{{vehiculeId}}/agence/{{agenceIdA}}"
# -> HTTP/1.1 200

curl -i -X PUT "http://localhost:8081/api/vehicules/{{vehiculeId}}/equipements/{{equipementId}}"
# -> HTTP/1.1 200

curl -i -X PUT "http://localhost:8081/api/agences/{{agenceIdA}}/employes" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Apres-Valide","ville":"Sfax","adresse":"2 rue W9","telephone":"74000000","employes":[{"nom":"Ben Salah","prenom":"Ali","role":"AGENT"},{"nom":"Trabelsi","prenom":"Sonia","role":"MANAGER"}]}'
# -> HTTP/1.1 200  {...,"employes":[...2...],"effectif":2}
```

### SQL — vérification atelier 9
```sql
SELECT id_vehicule, agence_id FROM vehicule WHERE id_vehicule = <vehiculeId>;
SELECT vehicule_id, equipement_id FROM vehicule_equipement;
SELECT id_employe, nom, agence_id FROM employe WHERE agence_id = <agenceIdA>;
SELECT id_agence, nom, effectif FROM agence WHERE id_agence = <agenceIdA>;
```
