# Documentation des APIs ColorRun

## Vue d'ensemble

Ce document décrit toutes les APIs REST disponibles dans l'application ColorRun. Toutes les réponses sont au format JSON et utilisent l'encodage UTF-8.

## Authentification

La plupart des APIs nécessitent une authentification via session. Les utilisateurs doivent d'abord se connecter via `/api/auth/login`.

## Codes de statut HTTP

- `200` : Succès
- `201` : Créé avec succès
- `204` : Succès sans contenu
- `400` : Requête invalide
- `401` : Non autorisé
- `403` : Interdit
- `404` : Non trouvé
- `409` : Conflit
- `500` : Erreur serveur

## 1. API d'authentification (`/api/auth/*`)

### POST `/api/auth/login`

Connexion d'un utilisateur.

**Body :**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Réponse :**

```json
{
  "success": true,
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "role": "PARTICIPANT"
  }
}
```

### POST `/api/auth/logout`

Déconnexion d'un utilisateur.

**Réponse :**

```json
{
  "success": true,
  "message": "Déconnexion réussie"
}
```

### POST `/api/auth/register`

Inscription d'un nouvel utilisateur.

**Body :**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "password": "password123"
}
```

**Réponse :**

```json
{
  "success": true,
  "message": "Compte créé avec succès. Vérifiez votre email pour activer votre compte."
}
```

### GET `/api/auth/profile`

Récupérer le profil de l'utilisateur connecté.

**Réponse :**

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "role": "PARTICIPANT",
  "verified": true
}
```

### POST `/api/auth/profile`

Mettre à jour le profil de l'utilisateur connecté.

**Body :**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "0123456789"
}
```

## 2. API des courses (`/api/courses/*`)

### GET `/api/courses`

Récupérer toutes les courses.

**Réponse :**

```json
[
  {
    "id": 1,
    "nom": "ColorRun Paris",
    "description": "Course colorée dans Paris",
    "date": "2024-06-15T09:00:00",
    "lieu": "Parc de la Villette",
    "prix": 25.0,
    "placesDisponibles": 100,
    "organisateurId": 2
  }
]
```

### GET `/api/courses/{id}`

Récupérer une course spécifique.

**Réponse :**

```json
{
  "id": 1,
  "nom": "ColorRun Paris",
  "description": "Course colorée dans Paris",
  "date": "2024-06-15T09:00:00",
  "lieu": "Parc de la Villette",
  "prix": 25.0,
  "placesDisponibles": 100,
  "organisateurId": 2
}
```

### POST `/api/courses`

Créer une nouvelle course (Organisateur/Admin uniquement).

**Body :**

```json
{
  "nom": "ColorRun Lyon",
  "description": "Course colorée à Lyon",
  "date": "2024-07-20T09:00:00",
  "lieu": "Parc de la Tête d'Or",
  "prix": 30.0,
  "placesDisponibles": 150
}
```

### PUT `/api/courses/{id}`

Modifier une course (Organisateur/Admin uniquement).

**Body :**

```json
{
  "nom": "ColorRun Lyon Modifié",
  "description": "Course colorée à Lyon - Édition spéciale",
  "date": "2024-07-20T09:00:00",
  "lieu": "Parc de la Tête d'Or",
  "prix": 35.0,
  "placesDisponibles": 200
}
```

### DELETE `/api/courses/{id}`

Supprimer une course (Organisateur/Admin uniquement).

## 3. API des utilisateurs (`/api/users/*`)

### GET `/api/users`

Récupérer tous les utilisateurs (Admin uniquement).

**Réponse :**

```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "role": "PARTICIPANT",
    "verified": true
  }
]
```

### GET `/api/users/{id}`

Récupérer un utilisateur spécifique (Admin uniquement).

### PUT `/api/users/{id}`

Modifier un utilisateur.

**Body :**

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "ORGANISATEUR"
}
```

### DELETE `/api/users/{id}`

Supprimer un utilisateur (Admin uniquement).

## 4. API des messages (`/api/messages/*`)

### GET `/api/messages?courseId={id}`

Récupérer les messages d'une course.

**Réponse :**

```json
[
  {
    "id": 1,
    "courseId": 1,
    "userId": 2,
    "contenu": "Super course !",
    "dateHeure": "2024-05-15T14:30:00",
    "auteur": "John Doe"
  }
]
```

### GET `/api/messages/{id}`

Récupérer un message spécifique.

### POST `/api/messages`

Créer un nouveau message.

**Body :**

```json
{
  "courseId": 1,
  "contenu": "Super course !"
}
```

### DELETE `/api/messages/{id}`

Supprimer un message (auteur ou admin uniquement).

## 5. API des participants (`/api/participants/*`)

### GET `/api/participants?courseId={id}`

Récupérer les participants d'une course.

**Réponse :**

```json
[
  {
    "id": 1,
    "courseId": 1,
    "userId": 2,
    "nom": "Doe",
    "prenom": "John"
  }
]
```

### GET `/api/participants/{id}`

Récupérer un participant spécifique.

### POST `/api/participants`

S'inscrire à une course.

**Body :**

```json
{
  "courseId": 1
}
```

### DELETE `/api/participants/{id}`

Se désinscrire d'une course (participant ou admin uniquement).

## 6. API des demandes d'organisateur (`/api/organizer-requests/*`)

### GET `/api/organizer-requests`

Récupérer toutes les demandes (Admin uniquement).

**Réponse :**

```json
[
  {
    "id": 1,
    "userId": 2,
    "motivation": "Je veux organiser des courses colorées",
    "status": "EN_ATTENTE",
    "requestDate": "2024-05-15T10:00:00",
    "userFirstName": "John",
    "userLastName": "Doe",
    "userEmail": "john@example.com"
  }
]
```

### GET `/api/organizer-requests/{id}`

Récupérer une demande spécifique (Admin uniquement).

### POST `/api/organizer-requests`

Créer une demande d'organisateur.

**Body :**

```json
{
  "motivation": "Je veux organiser des courses colorées"
}
```

### PUT `/api/organizer-requests/{id}`

Modifier une demande (Admin uniquement).

**Body :**

```json
{
  "motivation": "Je veux organiser des courses colorées",
  "status": "APPROUVEE"
}
```

### DELETE `/api/organizer-requests/{id}`

Supprimer une demande (Admin uniquement).

## Exemples d'utilisation

### Connexion et récupération des courses

```javascript
// 1. Connexion
const loginResponse = await fetch("/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    email: "user@example.com",
    password: "password123",
  }),
});

// 2. Récupération des courses
const coursesResponse = await fetch("/api/courses");
const courses = await coursesResponse.json();
```

### Inscription à une course

```javascript
const inscriptionResponse = await fetch("/api/participants", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    courseId: 1,
  }),
});
```

### Création d'une course (organisateur)

```javascript
const courseResponse = await fetch("/api/courses", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    nom: "ColorRun Marseille",
    description: "Course colorée à Marseille",
    date: "2024-08-15T09:00:00",
    lieu: "Vieux Port",
    prix: 25.0,
    placesDisponibles: 200,
  }),
});
```

## Gestion des erreurs

Toutes les APIs retournent des messages d'erreur au format JSON :

```json
{
  "error": "Message d'erreur descriptif"
}
```

Exemples d'erreurs courantes :

- `"Non autorisé"` : Utilisateur non connecté ou permissions insuffisantes
- `"Course non trouvée"` : L'ID de course n'existe pas
- `"Déjà inscrit à cette course"` : L'utilisateur est déjà inscrit
- `"Email déjà utilisé"` : L'email existe déjà lors de l'inscription
