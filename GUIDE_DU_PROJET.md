# 📘 Guide du projet — BudgetManager (version enrichie)

> Document d'explication complet du **code** et des **outils** utilisés.
> À lire par toute l'équipe pour comprendre le projet et **répondre aux questions de l'enseignant**.
>
> Application mobile Android · Langage **Java** · Base de données **SQLite** · Android Studio.

---

## 1. Vue d'ensemble

**BudgetManager** est une application de gestion de budget personnel **multi-écrans**. L'utilisateur enregistre ses **revenus** et **dépenses** (avec catégorie et date), consulte son **solde**, l'**historique**, des **statistiques graphiques**, et définit un **budget mensuel** avec alerte.

### Architecture générale

L'application est organisée autour d'**une activité hôte** (`MainActivity`) qui affiche **4 écrans** (Fragments) selon l'onglet choisi dans une **barre de navigation** :

```
┌──────────────────────────────────────────────┐
│              MainActivity (hôte)               │
│  ┌──────────────────────────────────────────┐ │
│  │   Fragment affiché (1 des 4 écrans)       │ │
│  └──────────────────────────────────────────┘ │
│  [🏠 Accueil][💳 Transactions][📊 Stats][⚙️ Budget] │ ← BottomNavigationView
└──────────────────────────────────────────────┘
```

Un écran séparé (`AddEditTransactionActivity`) sert à **ajouter ou modifier** une transaction.

---

## 2. Les outils et composants utilisés (IMPORTANT pour l'enseignant)

Ce tableau explique **chaque outil**, **à quoi il sert** et **où il est utilisé**. C'est la section clé pour la soutenance.

### 2.1 Composants Android de base (vus en cours)

| Outil | À quoi ça sert | Où dans le projet |
|-------|----------------|-------------------|
| **Activity** | Un écran (ou hôte d'écrans) | `MainActivity`, `AddEditTransactionActivity` |
| **Fragment** | Un « sous-écran » réutilisable affiché dans une activité | Accueil, Transactions, Statistiques, Budget |
| **LinearLayout** | Empile les composants (vertical/horizontal) | Tous les layouts |
| **TextView** | Afficher du texte | Solde, totaux, montants… |
| **EditText** | Saisir du texte / des nombres | Montant, description, recherche |
| **Button** | Déclencher une action | Enregistrer, Définir budget… |
| **RadioGroup / RadioButton** | Choix unique (Dépense **ou** Revenu) | Écran d'ajout |
| **Spinner** | Menu déroulant (choisir une catégorie) | Écran d'ajout, filtre |
| **ProgressBar** | Barre de progression (suivi du budget) | Accueil, Budget |
| **Toast** | Petit message temporaire | Confirmations, erreurs |
| **AlertDialog** | Boîte de dialogue (confirmer la suppression) | Suppression d'une transaction |

### 2.2 Composants plus avancés

| Outil | À quoi ça sert | Où dans le projet |
|-------|----------------|-------------------|
| **BottomNavigationView** | La barre de navigation du bas (4 onglets) | `MainActivity` + `bottom_nav_menu.xml` |
| **RecyclerView** | Afficher une liste performante d'éléments | Liste des transactions, dernières transactions |
| **Adapter (TransactionAdapter)** | Fait le lien entre les données et la RecyclerView | `TransactionAdapter.java` |
| **ViewHolder** | Mémorise les vues d'une ligne (performance) | Classe interne `VH` |
| **DatePickerDialog** | Sélecteur de date (calendrier) | Écran d'ajout |
| **FloatingActionButton (FAB)** | Le bouton « + » flottant | Écran Transactions |
| **ActivityResultLauncher** | Rafraîchir la liste au retour de l'écran d'ajout | `TransactionsFragment` |

### 2.3 Base de données

| Outil | À quoi ça sert | Où |
|-------|----------------|-----|
| **SQLite** | Base de données locale (sur le téléphone) | Toute l'app |
| **SQLiteOpenHelper** | Créer et gérer la base | `DatabaseHelper.java` |
| **ContentValues** | Préparer les données à insérer/modifier | `ajouterTransaction`, `modifierTransaction` |
| **Cursor** | Parcourir les résultats d'une requête `SELECT` | Toutes les lectures |

### 2.4 Bibliothèque externe

| Outil | À quoi ça sert | Où |
|-------|----------------|-----|
| **MPAndroidChart** | Dessiner les graphiques (camembert, barres) | `StatistiquesFragment` |

> MPAndroidChart est ajoutée dans `app/build.gradle` et récupérée via le dépôt **JitPack** (configuré dans `settings.gradle`).

---

## 3. Les fichiers du projet

### Classes Java (`app/src/main/java/com/example/budgetmanager/`)

| Fichier | Rôle | Membre |
|---------|------|--------|
| `MainActivity.java` | Écran hôte + navigation entre les 4 fragments | **KT** |
| `AccueilFragment.java` | Écran d'accueil (solde, résumé, budget, dernières transactions) | **KT** |
| `TransactionsFragment.java` | Liste, recherche, filtre, ajout, **modification**, **suppression** | **KT** |
| `StatistiquesFragment.java` | Graphiques camembert + barres | **KT** |
| `BudgetFragment.java` | Définir et suivre le budget mensuel | **KT** |
| `AddEditTransactionActivity.java` | Écran d'ajout / modification d'une transaction | **KT** |
| `DatabaseHelper.java` | Base de données SQLite (tables, CRUD, statistiques) | **MP** |
| `Transaction.java` | Modèle de données (une transaction) | **MP** |
| `TransactionAdapter.java` | Adaptateur de la RecyclerView | **KT** |
| `Utils.java` | Fonctions utilitaires (formatage montant, dates) | **KT** |

### Ressources (`app/src/main/res/`)

| Fichier | Rôle | Membre |
|---------|------|--------|
| `layout/activity_main.xml` | Conteneur + barre de navigation | **LP** |
| `layout/fragment_*.xml` | Les 4 écrans | **LP** |
| `layout/activity_add_edit.xml` | Formulaire d'ajout/modification | **LP** |
| `layout/item_transaction_card.xml` | Une ligne de la liste | **LP** |
| `menu/bottom_nav_menu.xml` | Les 4 onglets de navigation | **LP** |
| `values/colors.xml`, `strings.xml`, `themes.xml` | Couleurs, textes, thème | **LP** |
| `drawable/ic_*.xml` | Les icônes | **LP** |

---

## 4. La base de données en détail (responsable : MP)

**Base `budget_db`, version 2.** Deux tables :

### Table `transactions`
| Colonne | Type | Description |
|---------|------|-------------|
| id | INTEGER PK AUTOINCREMENT | Identifiant unique |
| montant | REAL | Le montant |
| type | TEXT | `"revenu"` ou `"depense"` |
| **categorie** | TEXT | Nourriture, Transport, Loyer… |
| description | TEXT | Libellé |
| date | TEXT | Format AAAA-MM-JJ |

### Table `budgets`
| Colonne | Type | Description |
|---------|------|-------------|
| id | INTEGER PK AUTOINCREMENT | Identifiant |
| mois | TEXT UNIQUE | Format AAAA-MM |
| montant_max | REAL | Budget maximum du mois |

**Méthodes principales** : `ajouterTransaction`, `modifierTransaction`, `supprimerTransaction`, `getTransactions` (avec filtres), `getTotalParType`, `getDepensesParCategorie` (camembert), `getTotauxParMois` (barres), `definirBudget`, `getBudget`.

> **Migration** : la base est passée de la version 1 à la version 2 via `onUpgrade()`, qui **ajoute la colonne `categorie`** et **crée la table `budgets`** sans détruire les données existantes (`ALTER TABLE`).

---

## 5. Comment fonctionne chaque écran

- **🏠 Accueil** : affiche le solde global, les revenus/dépenses du mois, la barre de budget, et les 5 dernières transactions. Tout est rechargé dans `onResume()`.
- **💳 Transactions** : liste toutes les transactions (RecyclerView). On peut **rechercher** (champ texte), **filtrer** (Spinner), **ajouter** (bouton +), **modifier** (clic sur une ligne) et **supprimer** (appui long + confirmation).
- **📊 Statistiques** : un **camembert** des dépenses par catégorie et un **graphique en barres** des revenus vs dépenses par mois (MPAndroidChart).
- **⚙️ Budget** : définir le budget du mois, voir le pourcentage utilisé et une **alerte** si dépassement.

### Règle de gestion importante
**Le solde ne peut pas devenir négatif** : si l'utilisateur saisit une dépense **supérieure au solde disponible**, l'application **refuse** l'opération avec le message *« Solde insuffisant »* (logique dans `AddEditTransactionActivity.enregistrer()`).

---

## 6. Questions probables de l'enseignant (et réponses)

**Q : Pourquoi des Fragments plutôt que plusieurs Activities ?**
> Un Fragment est un écran réutilisable géré par une activité hôte. Avec une `BottomNavigationView`, c'est l'approche moderne standard : on garde une seule activité et on change juste le Fragment affiché. C'est plus fluide et plus économe que de lancer une nouvelle activité à chaque onglet.

**Q : Différence entre ListView (version simple) et RecyclerView ?**
> La `RecyclerView` est plus performante : elle **recycle** les vues (via le `ViewHolder`) au lieu d'en créer une par élément. C'est la version moderne et recommandée de la `ListView`.

**Q : À quoi sert l'Adapter ?**
> Il fait le **pont** entre les données (la liste de `Transaction`) et l'affichage (la RecyclerView). Il crée une vue par ligne et y place les bonnes valeurs.

**Q : Comment fonctionne la navigation ?**
> La `BottomNavigationView` détecte l'onglet cliqué. Dans `MainActivity`, on remplace alors le Fragment affiché dans le conteneur via un `FragmentTransaction`.

**Q : Comment les graphiques sont-ils générés ?**
> On utilise la bibliothèque **MPAndroidChart**. On récupère les totaux depuis SQLite (par catégorie pour le camembert, par mois pour les barres) et on les transforme en `PieEntry` / `BarEntry`.

**Q : Comment les catégories sont-elles gérées ?**
> Une liste fixe de catégories est définie dans `DatabaseHelper.CATEGORIES`. À l'ajout, l'utilisateur la choisit via un `Spinner`. La catégorie est stockée dans la colonne `categorie`.

**Q : Comment empêchez-vous un solde négatif ?**
> Avant d'enregistrer une dépense, on calcule le solde disponible. Si le montant dépasse ce solde, on bloque avec un message d'erreur.

**Q : Comment la base évolue-t-elle sans perdre les données ?**
> Grâce à `onUpgrade()` : en passant de la version 1 à 2, on ajoute la colonne et la table manquantes avec `ALTER TABLE` / `CREATE TABLE`, sans supprimer l'existant.

**Q : Pourquoi `minSdk 24` alors que le cahier dit API 16 ?**
> Les bibliothèques modernes (AndroidX, Material, RecyclerView) ne sont plus compatibles avec l'API 16. On utilise donc API 24 (Android 7.0), qui couvre la quasi-totalité des téléphones actuels.

---

## 7. Comment lancer et démontrer l'application

1. Ouvrir le projet dans **Android Studio** et laisser **Gradle** se synchroniser.
2. Brancher un **téléphone** (débogage USB) ou lancer un **émulateur**.
3. Cliquer sur **▶️ Run**.
4. **Pour la présentation** : aller dans l'onglet **⚙️ Budget** → bouton **« Charger des données de démonstration »**. L'app se remplit de transactions réalistes → les graphiques, l'historique et le budget deviennent parlants.

> ⚠️ Le bouton de démonstration est **temporaire** (marqué `TEMPORAIRE` dans le code) : à retirer pour la version finale.

---

## 8. Mini-glossaire

- **Activity / Fragment** : un écran / un sous-écran réutilisable.
- **Layout** : la disposition des éléments (en XML).
- **Adapter / ViewHolder** : relient les données à une liste (RecyclerView).
- **SQLite / SQLiteOpenHelper** : base de données locale et sa classe de gestion.
- **Cursor** : parcourt les résultats d'une requête.
- **ContentValues** : prépare les valeurs à insérer.
- **Spinner** : menu déroulant.
- **FAB** : bouton d'action flottant.
- **MPAndroidChart** : bibliothèque de graphiques.
- **Persistance** : les données restent même après fermeture de l'app.

---

*Document rédigé pour l'équipe BudgetManager — IUT Fotso Victor de Bandjoun, Juin 2026.*
