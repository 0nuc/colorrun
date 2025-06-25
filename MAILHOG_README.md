# Configuration MailHog pour ColorRun

## Qu'est-ce que MailHog ?

MailHog est un serveur SMTP de test qui intercepte tous les emails envoyés par l'application et les affiche dans une interface web. C'est parfait pour le développement car vous n'avez pas besoin de configurer un vrai serveur SMTP.

## Installation et démarrage

### 1. Démarrage de MailHog

```bash
# Option 1: Utiliser le script batch
start-mailhog.bat

# Option 2: Lancer directement
./mailhog.exe
```

### 2. Accès à l'interface web

Une fois MailHog démarré, ouvrez votre navigateur et allez sur :
**http://localhost:8025**

Vous verrez tous les emails envoyés par l'application.

## Configuration de l'application

L'application est déjà configurée pour utiliser MailHog :

- **SMTP Host**: localhost
- **SMTP Port**: 1025
- **Authentification**: Désactivée

## Utilisation

1. **Démarrez MailHog** avec `start-mailhog.bat`
2. **Démarrez votre application** ColorRun
3. **Testez l'envoi d'emails** :
   - Inscription d'un nouvel utilisateur
   - Demande de reset de mot de passe
4. **Consultez les emails** sur http://localhost:8025

## Emails disponibles

- **Vérification de compte** : Envoyé lors de l'inscription
- **Reset de mot de passe** : Envoyé lors de la demande de reset

## Avantages

- ✅ Pas besoin de configurer un vrai serveur SMTP
- ✅ Interface web pour consulter les emails
- ✅ Parfait pour le développement et les tests
- ✅ Pas de spam dans votre vraie boîte mail

## Dépannage

Si les emails n'apparaissent pas dans MailHog :

1. Vérifiez que MailHog est démarré (port 1025)
2. Vérifiez que l'interface web est accessible (port 8025)
3. Regardez les logs de l'application pour les erreurs SMTP
