# 📘 Guide du projet — BudgetManager

> Document d'explication complet du **code** de l'application BudgetManager.
> À lire par toute l'équipe pour comprendre le projet et **répondre aux questions de l'enseignant**.
>
> Application mobile Android · Langage **Java** · Base de données **SQLite** · Android Studio.

---

## 1. Vue d'ensemble

**BudgetManager** est une application de gestion de budget personnel. L'utilisateur saisit ses **revenus** et ses **dépenses**, et l'application affiche en temps réel son **solde** ainsi que l'**historique** de toutes les transactions. Les données sont stockées **localement** dans une base SQLite (aucun Internet requis).

### Architecture (très simple, conforme au cours)

L'application repose sur **2 classes Java** + **1 écran XML** :

```
┌─────────────────────────┐        ┌──────────────────────────┐
│     MainActivity.java    │  utilise │    DatabaseHelper.java    │
│  (interface + logique)   │ ───────► │   (base de données SQLite)│
└───────────┬─────────────┘        └──────────────────────────┘
            │ affiche
            ▼
┌─────────────────────────┐
│    activity_main.xml     │
│   (l'écran / interface)  │
└─────────────────────────┘
```

- **MainActivity.java** = le cerveau : gère les clics, calcule le solde, affiche la liste.
- **DatabaseHelper.java** = la mémoire : enregistre et récupère les transactions dans SQLite.
- **activity_main.xml** = le visage : ce que l'utilisateur voit et touche.

---

## 2. Structure des fichiers du projet

| Fichier | Rôle | Membre responsable |
|---------|------|--------------------|
| `app/src/main/res/layout/activity_main.xml` | L'interface graphique (l'écran) | **LP** (Laura) |
| `app/src/main/res/values/strings.xml` | Tous les textes affichés | **LP** (Laura) |
| `app/src/main/res/values/colors.xml` | La palette de couleurs | **LP** (Laura) |
| `app/src/main/java/.../DatabaseHelper.java` | La base de données SQLite | **MP** (Paloma) |
| `app/src/main/java/.../MainActivity.java` | La logique de l'application | **KT** (Torres) |
| `app/src/main/AndroidManifest.xml` | La « carte d'identité » de l'app | **KT** (Torres) |

> Le **package** Java du projet est `com.example.budgetmanager`.

---

## 3. Explication détaillée de chaque fichier

### 3.1 `activity_main.xml` — L'interface (responsable : LP)

C'est un **`LinearLayout` vertical** (les éléments sont empilés de haut en bas). Il contient, dans l'ordre :

| Composant | Identifiant (`id`) | Rôle |
|-----------|--------------------|------|
| `TextView` | `tvSolde` | Affiche le solde actuel |
| `TextView` | `tvTotalRevenus` | Affiche le total des revenus |
| `TextView` | `tvTotalDepenses` | Affiche le total des dépenses |
| `EditText` | `etMontant` | Saisie du montant (clavier numérique) |
| `EditText` | `etDescription` | Saisie de la description |
| `RadioGroup` | `rgType` | Conteneur du choix du type |
| `RadioButton` | `rbDepense` | Option « Dépense » (cochée par défaut) |
| `RadioButton` | `rbRevenu` | Option « Revenu » |
| `Button` | `btnAjouter` | Valide et enregistre la transaction |
| `ListView` | `lvHistorique` | Affiche la liste des transactions |

**Points clés à expliquer :**
- L'`EditText` du montant utilise `android:inputType="numberDecimal"` → le clavier n'affiche que des chiffres.
- La `ListView` a `layout_height="0dp"` + `layout_weight="1"` → elle **occupe tout l'espace restant** en bas de l'écran.
- Un `RadioGroup` force un **choix unique** : on ne peut cocher que « Dépense » OU « Revenu », jamais les deux.

---

### 3.2 `DatabaseHelper.java` — La base de données (responsable : MP)

Cette classe **hérite de `SQLiteOpenHelper`** (la classe Android standard pour gérer une base SQLite).

**La base s'appelle `budget_db` et contient une seule table : `transactions`.**

| Colonne | Type SQL | Description |
|---------|----------|-------------|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Identifiant unique automatique |
| `montant` | REAL NOT NULL | Le montant (nombre à virgule) |
| `type` | TEXT NOT NULL | `"revenu"` ou `"depense"` |
| `description` | TEXT | Libellé (peut être vide) |
| `date` | TEXT NOT NULL | Date au format `AAAA-MM-JJ` |

**Les méthodes importantes :**

| Méthode | Ce qu'elle fait | SQL utilisé |
|---------|-----------------|-------------|
| `onCreate()` | Crée la table au 1er lancement | `CREATE TABLE ...` |
| `onUpgrade()` | Recrée la table si la version change | `DROP TABLE` + `onCreate` |
| `ajouterTransaction(...)` | Insère une transaction | `INSERT` (via `ContentValues`) |
| `getToutesTransactions()` | Récupère toutes les lignes | `SELECT * ... ORDER BY id DESC` |
| `supprimerTransaction(id)` | Supprime une transaction | `DELETE WHERE id = ?` |
| `getTotalParType(type)` | Somme des montants d'un type | `SELECT SUM(montant) WHERE type = ?` |

**Concepts à connaître :**
- **`ContentValues`** : un « panier » de paires colonne→valeur, utilisé pour insérer proprement (évite d'écrire le SQL à la main et protège contre les injections).
- **`Cursor`** : un « pointeur » qui parcourt les résultats d'un `SELECT`, ligne par ligne (`moveToNext()`).
- **`getWritableDatabase()` / `getReadableDatabase()`** : ouvrent la base en écriture / lecture.

---

### 3.3 `MainActivity.java` — La logique (responsable : KT)

C'est l'**activité unique** de l'application. Elle **étend `AppCompatActivity`**.

**Le cycle :**

1. **`onCreate()`** — au lancement :
   - récupère toutes les vues avec `findViewById(R.id.xxx)` ;
   - crée l'objet `DatabaseHelper` ;
   - prépare l'`ArrayAdapter` de la liste ;
   - branche les **listeners** (clic bouton, appui long liste) ;
   - appelle `actualiserListe()`.

2. **`ajouterTransaction()`** — au clic sur « Ajouter » :
   - lit le montant et la description ;
   - **vérifie** que le montant est valide (non vide, nombre, positif) → sinon `Toast` d'erreur ;
   - détermine le type selon le `RadioButton` coché ;
   - calcule la **date du jour** (`SimpleDateFormat`) ;
   - enregistre via `bdd.ajouterTransaction(...)` ;
   - vide les champs et rafraîchit la liste.

3. **`actualiserListe()`** — recharge l'affichage :
   - vide les listes locales ;
   - parcourt le `Cursor` de `getToutesTransactions()` ;
   - construit une ligne de texte par transaction + mémorise son `id` ;
   - prévient l'`ArrayAdapter` (`notifyDataSetChanged()`) ;
   - appelle `calculerSolde()`.

4. **`calculerSolde()`** — met à jour les 3 `TextView` :
   - **`Solde = Total Revenus − Total Dépenses`** (la règle de gestion du projet).

5. **`confirmerSuppression(position)`** — à l'appui long sur une ligne :
   - affiche une **boîte de dialogue** (`AlertDialog`) de confirmation ;
   - si « Oui » → `bdd.supprimerTransaction(id)` puis rafraîchit.

**Concepts à connaître :**
- **Listener** : un « écouteur » d'événement. Ex. `setOnClickListener` réagit au clic.
- **`ArrayAdapter`** : fait le lien entre une **liste de données** (ArrayList) et la **`ListView`** affichée.
- **`Toast`** : petit message temporaire en bas de l'écran.
- **`AlertDialog`** : boîte de dialogue (ici, confirmation de suppression).
- **Astuce technique** : on garde **deux listes parallèles** — `lignesAffichage` (le texte montré) et `idsTransactions` (l'id en base). À la même position, elles correspondent à la même transaction. C'est ce qui permet de savoir **quel id supprimer** quand on appuie sur une ligne.

---

## 4. Correspondance avec le cours (pour l'enseignant)

| Chapitre | Concept | Où, dans BudgetManager |
|----------|---------|------------------------|
| Ch. 3 | Activité & cycle de vie | `MainActivity`, méthode `onCreate()` |
| Ch. 4 | `LinearLayout` | `activity_main.xml` |
| Ch. 4 | `TextView`, `EditText`, `Button` | Solde, saisies, bouton |
| Ch. 4 | `RadioGroup` / `RadioButton` | Choix Dépense / Revenu |
| Ch. 4 | `ListView` + `ArrayAdapter` | Historique des transactions |
| Ch. 4 | Gestion des événements | `setOnClickListener`, `setOnItemLongClickListener` |
| Ch. 4 | Classe `R` (ressources) | `R.id.*`, `R.string.*`, `R.layout.*` |
| Ch. 5 | `SQLiteOpenHelper` | `DatabaseHelper` |
| Ch. 5 | `Cursor` | Parcours des résultats dans `actualiserListe()` |
| Ch. 5 | `ContentValues` | Insertion dans `ajouterTransaction()` |

---

## 5. Le déroulement complet (exemple concret)

**Scénario : l'utilisateur ajoute un revenu de 150 000.**

1. Il tape `150000` dans le montant, `Salaire` dans la description, coche **Revenu**, clique sur **Ajouter**.
2. `MainActivity.ajouterTransaction()` valide le montant, lit le type `"revenu"`, calcule la date.
3. Appel à `DatabaseHelper.ajouterTransaction(150000, "revenu", "Salaire", "2026-06-25")` → ligne insérée dans SQLite.
4. `actualiserListe()` relit la base → la nouvelle ligne apparaît en haut de la `ListView`.
5. `calculerSolde()` recalcule : `Solde = revenus − dépenses` → le `tvSolde` se met à jour.

**Pour supprimer :** appui **long** sur une ligne → boîte de dialogue → « Oui » → la transaction disparaît et le solde se recalcule.

---

## 6. Questions probables de l'enseignant (et réponses)

**Q : Pourquoi une seule activité ?**
> Le cahier des charges impose une architecture simple à activité unique. Toute la logique tient dans `MainActivity`, ce qui suffit pour le périmètre demandé.

**Q : Comment les données sont-elles conservées après fermeture de l'app ?**
> Grâce à **SQLite** : les transactions sont écrites sur le disque du téléphone (base `budget_db`). Elles sont toujours là à la réouverture. C'est la **persistance**.

**Q : À quoi sert `SQLiteOpenHelper` ?**
> C'est la classe Android qui gère le cycle de vie de la base : sa **création** (`onCreate`) et ses **mises à jour** (`onUpgrade`). On en hérite dans `DatabaseHelper`.

**Q : Différence entre `ContentValues` et `Cursor` ?**
> `ContentValues` sert à **écrire** (on y met les valeurs à insérer). `Cursor` sert à **lire** (il parcourt les résultats d'un `SELECT`).

**Q : Comment le solde est-il calculé ?**
> `Solde = Total des revenus − Total des dépenses`. On calcule chaque total avec une requête `SELECT SUM(montant) WHERE type = ?`.

**Q : Comment fonctionne la suppression ?**
> Un `setOnItemLongClickListener` sur la `ListView` détecte l'appui long. On retrouve l'`id` de la transaction (liste parallèle `idsTransactions`), on demande confirmation via un `AlertDialog`, puis on appelle `DELETE`.

**Q : Pourquoi `minSdk 24` alors que le cahier dit API 16 ?**
> Les bibliothèques modernes (AndroidX) ne sont plus compatibles avec l'API 16. On utilise donc une version minimale réaliste (API 24, Android 7.0), ce qui couvre la quasi-totalité des téléphones actuels. Le reste du code suit bien le cahier des charges.

---

## 7. Comment lancer l'application

1. Ouvrir le projet dans **Android Studio**.
2. Laisser **Gradle** se synchroniser (barre en bas).
3. Brancher un **téléphone Android** (débogage USB activé) **ou** lancer un **émulateur**.
4. Cliquer sur **▶️ Run**.
5. Tester : ajouter un revenu, une dépense, vérifier le solde, supprimer par appui long.

---

## 8. Mini-glossaire

- **Activité (Activity)** : un écran de l'application.
- **Layout** : la disposition des éléments à l'écran (défini en XML).
- **Widget** : un composant d'interface (bouton, texte, champ…).
- **SQLite** : base de données légère intégrée à Android.
- **CRUD** : Create, Read, Update, Delete (créer, lire, modifier, supprimer).
- **Listener** : code qui réagit à un événement (clic, appui long…).
- **Toast** : court message temporaire à l'écran.
- **Persistance** : conservation des données même après fermeture de l'app.

---

*Document rédigé pour l'équipe BudgetManager — IUT Fotso Victor de Bandjoun, Juin 2026.*
