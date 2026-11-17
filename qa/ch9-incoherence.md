# Atelier 9 — étapes 15-16 : incohérence sans `@Transactional` (avant correctif)

Application démarrée **avant** l'ajout de `@Transactional` sur
`AgenceServiceImpl.updateAvecEmployes` (port 8090, profil MySQL par défaut).
Toutes les sorties ci-dessous sont les sorties brutes des commandes exécutées
(requests HTTP via `Invoke-WebRequest`, SQL via `SqlTool`).

## Requête exécutée (étape 15)

```
PUT http://localhost:8090/api/agences/10/employes
body: {"nom":"Apres-Invalide 956836","ville":"Monastir","adresse":"4 rue B2","telephone":"73500000","employes":[{"nom":"Ben Salah","prenom":"Ali","role":"AGENT"},{"prenom":"SansNom","role":"AGENT"}]}
```

Le **2e employé** n'a pas de `nom` (données invalides mais lisibles en JSON —
c'est bien un rejet **dans le service**, pas une erreur de désérialisation).

## SQL AVANT la requête invalide (agence 10, fixture fraîche)

```
SQL> SELECT id_agence, nom, ville, effectif FROM agence WHERE id_agence=10
  id_agence=10  nom=Agence B2-inval 956836  ville=Sousse  effectif=0
  (1 ligne(s))
SQL> SELECT COUNT(*) AS employes_avant FROM employe WHERE agence_id=10
  employes_avant=0
  (1 ligne(s))
```

## Réponse HTTP (étape 15)

```
<<< HTTP 400
{
  "timestamp": "2026-11-17T14:21:42.8191241",
  "status": 400,
  "error": "Bad Request",
  "message": "La vérification de l'entité a échoué.",
  "path": "/api/agences/10/employes"
}
```

## SQL APRÈS la requête invalide (étape 16 — l'incohérence)

```
SQL> SELECT id_employe, nom, prenom, agence_id FROM employe WHERE agence_id=10
  id_employe=3  nom=Ben Salah  prenom=Ali  agence_id=10
  (1 ligne(s))
SQL> SELECT id_agence, nom, ville, effectif FROM agence WHERE id_agence=10
  id_agence=10  nom=Apres-Invalide 956836  ville=Monastir  effectif=0
  (1 ligne(s))
```

**Incohérence constatée (aucune transaction englobante) :**

| Élément | État après l'échec | Attendu (rollback) |
|---|---|---|
| 1er employé (`Ben Salah`) | ✅ **inséré** (`id_employe=3`, `agence_id=10`) | pas inséré |
| 2e employé (sans `nom`) | ❌ rejeté par le check de `EmployeServiceImpl` | pas inséré |
| Champs de l'agence | ✅ **mis à jour** (`nom=Apres-Invalide…`, `ville=Monastir`) | inchangés |
| `effectif` | ❌ **0** alors que 1 employé existe (le `setEffectif` n'est jamais atteint) | inchangé |

L'échec du 2e employé n'a **rien annulé** : chaque écriture passait dans sa
propre transaction (`update`, puis `employeService.create` pour le 1er employé).

## État vu par le client (GET, étape 16)

```
GET /api/agences/10 -> HTTP 200
{"idAgence":10,"nom":"Apres-Invalide 956836","ville":"Monastir","adresse":"4 rue B2","employes":[{"idEmploye":3,"nom":"Ben Salah","prenom":"Ali","role":"AGENT"}],"effectif":0}
```

L'agence « à jour » affiche 1 employé mais `effectif = 0` (et, effet de
l'étape 13, l'employé sérialisé n'embarque plus l'objet `agence` —
`@JsonBackReference("agence-employes")`).

## Correction (étape 17)

`@Transactional` ajouté sur `AgenceServiceImpl.updateAvecEmployes` : la méthode
entière + les `create` des employés (runtime, `REQUIRED`) partagent la même
transaction → tout est annulé à la première exception. Le rejeu (avant/après
correctif) est documenté dans `qa/report-ateliers-8-9.md`.
