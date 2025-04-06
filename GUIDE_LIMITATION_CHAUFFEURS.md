# Guide d'utilisation : Limitation des chauffeurs par pack d'abonnement

## Fonctionnalités implémentées

Nous avons mis en place un système complet pour limiter l'accès des chauffeurs en fonction du pack d'abonnement choisi par l'entreprise :

1. **Limitation du nombre total de chauffeurs par entreprise** :
   - Pack START : 5 chauffeurs maximum
   - Pack PRO : 15 chauffeurs maximum
   - Pack ENTREPRISE : 50 chauffeurs maximum

2. **Limitation des connexions simultanées** :
   - Pack START : 3 chauffeurs connectés simultanément
   - Pack PRO : 10 chauffeurs connectés simultanément
   - Pack ENTREPRISE : 30 chauffeurs connectés simultanément

3. **Notifications automatiques** :
   - Notification par email lorsque le seuil d'alerte est atteint (80-90% selon le pack)
   - Email lorsque la limite de chauffeurs est atteinte
   - Email lorsque la limite de connexions simultanées est atteinte
   - Email en cas de problème d'abonnement

## Utilisation technique

### Gestion des plans tarifaires

Les plans tarifaires sont définis dans la base de données via l'entité `PlanTarifaireEntity`. Ils sont automatiquement initialisés au démarrage de l'application dans la méthode `initialiserPlansParDefaut()` du service `PlanTarifaireService`.

Pour modifier les limites des packs, vous pouvez :
1. Modifier directement les valeurs dans le code (méthode `initialiserPlansParDefaut()`)
2. Utiliser l'API REST `/plans` pour gérer les plans tarifaires :
   - `GET /plans` : Récupérer tous les plans
   - `GET /plans/{id}` : Récupérer un plan par ID
   - `GET /plans/code/{code}` : Récupérer un plan par code
   - `POST /plans` : Créer un nouveau plan
   - `PUT /plans/{id}` : Mettre à jour un plan existant

### Fonctionnement du contrôle d'accès

Le contrôle d'accès des chauffeurs se fait au moment de la connexion :

1. Le chauffeur envoie ses identifiants à l'API `/auth/chauffeur/login`
2. Le service `AuthenService` vérifie les identifiants et appelle `chauffeurConnexionService.verifierPossibiliteConnexion()`
3. Cette méthode vérifie :
   - Si l'entreprise a un abonnement actif
   - Si le nombre total de chauffeurs ne dépasse pas la limite du pack
   - Si le nombre de connexions simultanées ne dépasse pas la limite du pack
4. Si une des vérifications échoue, une exception `ForbiddenException` est levée avec un message approprié
5. Un email est envoyé à l'administrateur de l'entreprise pour l'informer du problème

### Suivi des connexions

Le système maintient une liste des chauffeurs connectés pour chaque entreprise :
- Les connexions sont enregistrées lors de l'authentification réussie
- Les déconnexions sont enregistrées via l'endpoint `/auth/chauffeur/logout`
- Les sessions inactives depuis plus d'une heure sont automatiquement nettoyées

### Gestion des notifications

Pour éviter d'envoyer trop d'emails aux administrateurs, le système limite les notifications :
- Un email par type d'alerte au maximum toutes les 24 heures
- Les emails contiennent des informations précises sur la situation et les actions à entreprendre

## Exemple de messages d'erreur

### Lorsque la limite du nombre total de chauffeurs est atteinte :
```
Le nombre maximum de chauffeurs autorisés pour votre entreprise est atteint. Contactez votre administrateur.
```

### Lorsque la limite de connexions simultanées est atteinte :
```
Le nombre maximum de connexions simultanées est atteint pour votre entreprise. Veuillez réessayer plus tard ou contactez votre administrateur.
```

### Lorsque l'entreprise n'a pas d'abonnement actif :
```
Votre entreprise n'a pas d'abonnement actif. Contactez votre administrateur.
```

## Modifications apportées

1. Création de nouvelles entités et DTOs :
   - PlanTarifaireEntity
   - PlanTarifaireDto

2. Création de nouveaux services :
   - PlanTarifaireService
   - ChauffeurConnexionService

3. Modification des services existants :
   - AuthenService (ajout de la vérification lors de la connexion)
   - EmailService (ajout de méthodes pour envoyer des emails de notification)

4. Ajout d'endpoints :
   - `/plans` (gestion des plans tarifaires)
   - `/auth/chauffeur/logout` (déconnexion explicite)

## Conclusion

Ce système permet de garantir que chaque entreprise utilise les ressources correspondant à son abonnement. Il offre également une expérience utilisateur claire en cas d'erreur et informe proactivement les administrateurs des problèmes potentiels. 