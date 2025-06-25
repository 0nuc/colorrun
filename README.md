# Objectifs principaux

- **Créer une application web responsive** permettant de promouvoir et gérer les courses Color Run.
- **Proposer des fonctionnalités interactives** pour les participants, organisateurs et administrateurs.
- **Assurer une expérience utilisateur intuitive et visuellement attrayante**.

# Ce qu'on attend de vous

1. **Wireframes** :
   - Réaliser les wireframes des différentes interfaces.
   - Le wireframe de la page d'accueil est fourni. Les étudiants devront concevoir les wireframes pour les autres pages.
2. **Intégration responsive** :
   - Réaliser le développement fonctionnel de cette application en développant toutes les fonctionnalités listées ci-dessous
   - Assurer que l'application est parfaitement adaptée aux écrans mobiles, tablettes et ordinateurs.
   - Le design doit véhiculer le fun de la course colorée.

# Fonctionnalités détaillées

### 1. **Connexion**

- Authentification via email et mot de passe.
- Option "Mot de passe oublié".
- Redirection en fonction du rôle (participant, organisateur, admin).

### 2. **Inscription**

- Formulaire pour créer un compte (nom, prénom, email, mot de passe, rôle souhaité par défaut : participant).
- Vérification par email pour valider le compte.

### 3. **Liste des courses**

- Affichage de toutes les courses à venir sous forme de cartes ou liste.
- **Filtres** :
  - Recherche par date.
  - Recherche par ville.
  - Recherche par distance (km).
- **Tri** : Trier les courses par date, par ville ou par distance.
- Bouton pour accéder aux détails de chaque course.

### 4. **Détails d'une course**

- Informations à afficher :
  - Nom de la course.
  - Description.
  - Date et heure.
  - Lieu (avec carte intégrée).
  - Distance.
  - Nombre maximum de participants.
  - Prix de participation.
  - ? Obstacles ou sans obstacles.
  - ? Cause soutenue par la course.
- Liste des participants inscrits.
- **Fil de discussion** :
  - Les participants peuvent poser des questions et discuter.
  - Messages affichés avec nom et heure d'envoi.

### 5. **Participation à une course**

- Les utilisateurs connectés peuvent s'inscrire à une course via un bouton.
- Une fois inscrits :
  - Génération automatique d'un PDF avec un **dossard personnalisé** (nom du participant, numéro de dossard, nom de la course). Le dossard peut intégrer un QR code qui permet de le vérifier ?
  - Téléchargement direct du PDF ou envoi par email.

### 6. **Affichage / modification du profil**

- Afficher les informations utilisateur (nom, prénom, email, rôle, liste des courses auxquelles il participe).
- Modifier les informations personnelles (sauf email).
- Ajouter ou modifier une photo de profil.

### 7. **Devenir organisateur**

- Les utilisateurs peuvent demander à devenir organisateur via un formulaire :
  - Champ texte pour expliquer leur motivation.
  - Cette demande sera visible par les administrateurs, qui peuvent l'approuver ou la refuser.
- Une fois approuvé, l'utilisateur obtient les droits d'organisateur.

### 8. **Gestion des rôles**

- **Super admin** :
  - Gère les utilisateurs (ajouter, supprimer, modifier des rôles).
  - Valide ou rejette les demandes pour devenir organisateur.
  - Supprime ou modifie des courses.
- **Organisateurs** :
  - Créent et gèrent leurs propres courses.
  - Consultent la liste des participants.
  - Gèrent le fil de discussion pour leurs courses.
- **Participants** :
  - Recherchent et participent aux courses.
  - Consultent leurs profils et leurs inscriptions.
  - Interagissent dans les fils de discussion.

# Technos à utiliser

### Front end (encadré par AH)

- Vous pouvez utiliser des libraries CSS type Bootstrap ou TailwindCSS.
- Vous **ne** pouvez **pas** utiliser de bibliothèques de composants tous faits (type Flowbite)
- Le code HTML CSS est à intégrer dans les vues Thymeleaf
- Vous pouvez utiliser des librairies pour ajouter des animations ou interactions, maps, …

### Back end (encadré par FX)

- Serveur Tomcat https://tomcat.apache.org/
- Servlets https://jakarta.ee/specifications/servlet/6.0/
- Une base de données H2 https://www.h2database.com/html/main.html
- Pas de spring boot

<aside>
💡

Un doute sur ce que vous voulez utiliser ? Demandez aux formateurs encadrant le projet !

</aside>
