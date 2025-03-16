# Configuration des variables d'environnement pour Stripe

Pour des raisons de sécurité, les clés API Stripe ne sont pas stockées dans le code source.
Vous devez configurer les variables d'environnement suivantes sur votre serveur de production :

```
STRIPE_API_KEY=votre_cle_api_stripe
STRIPE_WEBHOOK_SECRET=votre_cle_secrete_webhook
APP_FRONTEND_URL=url_de_votre_frontend
```

## Configuration pour le développement local

Pour le développement local, vous pouvez créer un fichier `.env` à la racine du projet avec ces variables.

## Configuration pour la production

En production, configurez ces variables d'environnement sur votre serveur ou dans votre système de déploiement.

### Exemple avec systemd

Si vous utilisez systemd, vous pouvez ajouter ces variables dans le fichier de service :

```
[Service]
Environment="STRIPE_API_KEY=votre_cle_api_stripe"
Environment="STRIPE_WEBHOOK_SECRET=votre_cle_secrete_webhook"
Environment="APP_FRONTEND_URL=url_de_votre_frontend"
```

### Exemple avec Docker

Si vous utilisez Docker, vous pouvez passer ces variables lors du lancement du conteneur :

```bash
docker run -e STRIPE_API_KEY=votre_cle_api_stripe -e STRIPE_WEBHOOK_SECRET=votre_cle_secrete_webhook -e APP_FRONTEND_URL=url_de_votre_frontend votre_image
```

## Sécurité

Ne stockez jamais les clés API Stripe dans le code source ou dans des fichiers versionnés.
Les fichiers `.env` sont exclus du versionnement via `.gitignore`. 