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

*(à compléter après la partie B — étapes 1 à 19)*
