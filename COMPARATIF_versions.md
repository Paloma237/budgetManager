# 🔄 Comparatif — Version simple vs Version enrichie

Ce document compare **les deux versions** de l'application développées :

- **Version 1 (simple)** : application minimale conforme au cahier des charges initial — **une seule page**.
- **Version 2 (enrichie)** : application complète **multi-écrans** avec catégories, statistiques et budget.

> Les deux sont en **Java + SQLite** sous Android Studio. La version 2 a été construite **par-dessus** la version 1, en conservant toutes les fonctionnalités de base.

---

## 1. Vue d'ensemble

| | **Version 1 (simple)** | **Version 2 (enrichie)** |
|---|---|---|
| Nombre d'écrans | **1 seul** écran | **4 écrans** + écran d'ajout |
| Navigation | Aucune | **BottomNavigationView** (4 onglets) |
| Architecture | 1 activité (`MainActivity`) | 1 activité hôte + **4 Fragments** + activité d'ajout |
| Affichage des listes | `ListView` + `ArrayAdapter` | `RecyclerView` + adaptateur personnalisé |
| Nombre de classes Java | 2 | **12** |

---

## 2. Fonctionnalités

| Fonctionnalité | Version 1 | Version 2 |
|----------------|:---------:|:---------:|
| Ajouter une transaction | ✅ | ✅ |
| Afficher le solde | ✅ | ✅ |
| Historique des transactions | ✅ | ✅ |
| Supprimer (appui long) | ✅ | ✅ |
| Persistance SQLite | ✅ | ✅ |
| **Catégories** (Nourriture, Transport…) | ❌ | ✅ |
| **Modifier** une transaction | ❌ | ✅ |
| **Choisir la date** (calendrier) | ❌ (date du jour) | ✅ (DatePicker) |
| **Recherche** dans l'historique | ❌ | ✅ |
| **Filtre** par type (revenu/dépense) | ❌ | ✅ |
| **Statistiques graphiques** (camembert + barres) | ❌ | ✅ |
| **Budget mensuel** + alerte de dépassement | ❌ | ✅ |
| **Résumé du mois** (revenus/dépenses) | ❌ | ✅ |
| **Blocage du solde négatif** | ❌ | ✅ |
| Données de démonstration | ❌ | ✅ |

---

## 3. Base de données

| | **Version 1** | **Version 2** |
|---|---|---|
| Tables | 1 (`transactions`) | 2 (`transactions` + `budgets`) |
| Colonnes de `transactions` | id, montant, type, description, date | + **categorie** |
| Requêtes statistiques | ❌ | ✅ (par catégorie, par mois) |
| Version de la base | 1 | 2 (avec migration `onUpgrade`) |

---

## 4. Concepts Android en plus dans la version 2

La version enrichie introduit des notions plus avancées (au-delà du cours de base) :

| Concept | Rôle |
|---------|------|
| **Fragment** | Chaque écran (Accueil, Transactions…) est un Fragment réutilisable |
| **BottomNavigationView** | La barre de navigation du bas |
| **RecyclerView** | Liste performante (remplace ListView) |
| **Spinner** | Menu déroulant pour choisir la catégorie |
| **DatePickerDialog** | Sélecteur de date (calendrier) |
| **FloatingActionButton** | Le bouton « + » flottant |
| **ActivityResultLauncher** | Rafraîchir la liste au retour de l'écran d'ajout |
| **MPAndroidChart** (bibliothèque externe) | Les graphiques camembert et barres |

---

## 5. En résumé

- **Version 1** = le **strict nécessaire** demandé par le cahier des charges. Simple, claire, parfaite pour démontrer la maîtrise des **bases** (Activity, ListView, SQLite).
- **Version 2** = une **vraie application aboutie**, qui montre une **architecture moderne** (Fragments, RecyclerView), une **meilleure expérience utilisateur** (navigation, catégories, recherche) et des **fonctionnalités avancées** (graphiques, budget).

> 💡 **Pour la soutenance** : la version 1 prouve que les fondamentaux du cours sont maîtrisés ; la version 2 prouve la **capacité à aller plus loin** et à concevoir une application complète. Les deux se complètent.

---

*Document de comparaison — Projet BudgetManager, Juin 2026.*
