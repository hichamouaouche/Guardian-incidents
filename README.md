<p align="center">
  <img src="rapport/logo_enset.png" alt="ENSET Mohammedia" height="80">
</p>

<h1 align="center">🛡️ Guardian Incidents</h1>

<p align="center">
  <b>Plateforme de gestion des incidents informatiques et de cybersécurité</b><br>
  Authentification et autorisation avec Spring Boot, Spring Security, JWT et React
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Security-6-6DB33F?logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/JWT-HS256-black?logo=jsonwebtokens" alt="JWT">
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black" alt="React">
  <img src="https://img.shields.io/badge/Vite-6-646CFF?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/Postman-94%20assertions%20OK-FF6C37?logo=postman&logoColor=white" alt="Postman">
</p>

Projet du module **Sécurité des systèmes distribués** (SDIA, 2e année), à l'**ENSET Mohammedia** (Université Hassan II de Casablanca).

- Réalisé par : **Hicham Ouaouche**
- Encadré par : **Pr. Abdelmajid BOUSSELHAM**

L'application permet aux employés d'une entreprise de déclarer des incidents (malware, phishing, panne réseau, fuite de données…). Les techniciens les traitent et l'administrateur supervise le tout.

## Aperçu

| Tableau de bord ADMIN | Traitement d'un incident (TECHNICIAN) |
|:---:|:---:|
| <img src="rapport/captures/C01_admin_dashboard.png" alt="Dashboard admin"> | <img src="rapport/captures/D03_technicien_traitement.png" alt="Traitement technicien"> |
| **Tableau de bord EMPLOYEE** | **Session expirée (JWT expiré)** |
| <img src="rapport/captures/B01_employee_dashboard.png" alt="Dashboard employé"> | <img src="rapport/captures/E01_session_expiree.png" alt="Session expirée"> |

## Architecture

<p align="center">
  <img src="rapport/diagrammes/architecture.png" alt="Architecture générale" width="85%">
</p>

<details>
<summary><b>Diagramme des entités</b></summary>
<p align="center"><img src="rapport/diagrammes/entites.png" alt="Diagramme des entités" width="85%"></p>
</details>

<details>
<summary><b>Flux d'authentification JWT</b></summary>
<p align="center"><img src="rapport/diagrammes/flux_jwt.png" alt="Flux JWT" width="85%"></p>
</details>

---

## 1. Technologies

| Couche | Technologies |
|--------|--------------|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA (Hibernate), Bean Validation, JJWT 0.12.6, Lombok |
| Base de données | H2 en mémoire (par défaut), MySQL (profil `mysql`) |
| Frontend | React 18, Vite 6, React Router 6, Axios |
| Tests | JUnit 5 + MockMvc, Postman, curl |

## 2. Arborescence

```
Guardian-incidents/
├── README.md
├── backend/                                  # Projet Spring Boot (Maven)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/sdia/incidents/
│       │   ├── IncidentsApplication.java
│       │   ├── config/          # SecurityConfig (CORS, STATELESS, règles), DataInitializer
│       │   ├── controller/      # Auth, Profile, Incident, Admin, Dashboard
│       │   ├── dto/             # Requêtes et réponses (records + @Valid)
│       │   ├── entity/          # User, Incident, IncidentHistory, IncidentComment, RefreshToken
│       │   ├── enums/           # Role, Category, Priority, Status
│       │   ├── exception/       # Exceptions métier + GlobalExceptionHandler (@RestControllerAdvice)
│       │   ├── repository/      # Interfaces Spring Data JPA
│       │   ├── security/        # JwtService, JwtAuthenticationFilter, EntryPoint 401, AccessDenied 403
│       │   └── service/         # Logique métier et contrôle propriétaire / affectation
│       ├── main/resources/      # application.properties, application-mysql.properties
│       └── test/java/com/sdia/incidents/
│           └── SecurityIntegrationTest.java   # 9 tests d'intégration (MockMvc)
├── frontend/                                 # Projet React (Vite)
│   ├── package.json, vite.config.js, index.html, .env
│   ├── public/
│   └── src/
│       ├── components/      # Navbar, ProtectedRoute, RoleRoute, Badge, IncidentTable
│       ├── context/         # AuthContext
│       ├── pages/           # Login, Register, Dashboard, Profile, MyIncidents, NewIncident,
│       │                    # IncidentDetails, AssignedIncidents, AdminUsers, AdminIncidents,
│       │                    # Forbidden, NotFound
│       ├── services/        # api.js (intercepteurs Axios), authService, incidentService, userService
│       └── App.jsx, main.jsx, index.css
└── rapport/
    ├── Rapport_Projet_JWT_Spring_Security_React.docx et pdf
    ├── logo_enset.png
    ├── diagrammes/          # architecture.png, entites.png, flux_jwt.png
    └── captures/            # captures de l'application et du terminal
        └── postman/         # captures des tests réalisés dans Postman
```

## 3. Lancer le projet

### Prérequis
- Java 21 et Maven 3.9 ou plus
- Node.js 18 ou plus et npm

### 3.1 Backend (port 8085)

```bash
cd backend
mvn spring-boot:run
# ou : mvn clean package && java -jar target/incidents-backend-1.0.0.jar
```

> Le port **8085** est utilisé à la place de 8080, qui était déjà occupé sur la machine de développement. Pour le modifier, changez `server.port` dans `backend/src/main/resources/application.properties` et `VITE_API_URL` dans `frontend/.env`.

Pour utiliser **MySQL** au lieu de H2 :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql   # voir application-mysql.properties
```

### 3.2 Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Ouvrez ensuite http://localhost:5173

### 3.3 Tests

```bash
# Tests d'intégration JUnit/MockMvc
cd backend && mvn test

```

Les tests Postman (scénario + 9 tests de sécurité obligatoires) ont été réalisés dans l'application Postman ; les captures se trouvent dans `rapport/captures/postman/` et dans la section 8 du rapport.

> Repartir d'une base vide avant de rejouer le scénario : la base H2 est en mémoire, il suffit de redémarrer le backend.

## 4. Ports utilisés

| Service | URL |
|---------|-----|
| API Spring Boot | http://localhost:8085 |
| Console H2 | http://localhost:8085/h2-console (JDBC URL : `jdbc:h2:mem:incidentsdb`, user `sa`, pas de mot de passe) |
| Application React | http://localhost:5173 |

## 5. Comptes de test

Les comptes ADMIN et TECHNICIAN sont créés au démarrage par `DataInitializer`, car l'inscription donne toujours le rôle EMPLOYEE.

| Rôle | Nom | Email | Mot de passe |
|------|-----|-------|--------------|
| ADMIN | Hicham Ouaouche | hichamouaouche@gmail.com | `Admin@2026` |
| TECHNICIAN | Youssef Amrani | tech1@incidents.ma | `Tech@2026` |
| TECHNICIAN | Karim Idrissi | tech2@incidents.ma | `Tech@2026` |
| EMPLOYEE | (à créer via /register) | ex. ahmed.benali@entreprise.ma | ex. `Password123` |

## 6. Rôles

| Rôle | Droits |
|------|--------|
| **EMPLOYEE** | Profil (lecture et modification), création d'incidents, consultation de **ses propres** incidents uniquement |
| **TECHNICIAN** | Consultation des incidents **qui lui sont affectés**, changement de statut (IN_PROGRESS, RESOLVED), ajout de notes de traitement |
| **ADMIN** | Tous les incidents, tous les utilisateurs, activation et désactivation de comptes, affectation aux techniciens, clôture, statistiques |

## 7. Principaux endpoints

### Authentification (publique)
| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/auth/register` | Inscription (rôle EMPLOYEE imposé) |
| POST | `/auth/login` | Connexion : renvoie `token`, `refreshToken`, `role`, `fullName` |
| POST | `/auth/refresh` | Nouvel access token à partir du refresh token (avec rotation) |
| POST | `/auth/logout` | Révocation du refresh token |

### Profil (authentifié)
| Méthode | URL |
|---------|-----|
| GET / PUT | `/api/profile` |
| PUT | `/api/profile/password` |

### Incidents
| Méthode | URL | Accès |
|---------|-----|-------|
| POST | `/api/incidents` | EMPLOYEE |
| GET | `/api/incidents/my` | EMPLOYEE |
| GET | `/api/incidents/{id}` | Propriétaire (EMPLOYEE), technicien affecté, ADMIN |
| GET | `/api/incidents/assigned` | TECHNICIAN |
| PUT | `/api/incidents/{id}/status` | TECHNICIAN (si affecté) ou ADMIN |
| PUT | `/api/incidents/{id}/assign/{technicianId}` | ADMIN |
| POST | `/api/incidents/{id}/comments` | TECHNICIAN (si affecté) ou ADMIN |
| GET | `/api/incidents?status=OPEN` | ADMIN |
| GET | `/api/dashboard` | Authentifié (statistiques selon le rôle) |

### Administration (ADMIN)
| Méthode | URL |
|---------|-----|
| GET | `/api/admin/users` |
| GET | `/api/admin/technicians` |
| PUT | `/api/admin/users/{id}/enable` |
| PUT | `/api/admin/users/{id}/disable` |
| GET | `/api/admin/statistics` |

## 8. Fonctionnalités réalisées

- [x] Authentification JWT (HS256). Claims : `sub` (email), `role`, `fullName`, `iat`, `exp`. Durée de vie de **5 minutes**
- [x] Refresh token (24 h) stocké en base, avec rotation et révocation à la déconnexion
- [x] Mots de passe hachés avec **BCrypt**
- [x] Spring Security en `SessionCreationPolicy.STATELESS`, CORS limité à `http://localhost:5173`
- [x] Autorisation à deux niveaux : règles d'URL (`/api/admin/**` → `hasRole('ADMIN')`) et `@PreAuthorize` sur chaque endpoint
- [x] **Contrôle du propriétaire** côté serveur (un employé ne peut pas lire l'incident d'un autre, même avec Postman)
- [x] Un technicien ne modifie que les incidents qui lui sont affectés
- [x] Machine à états des statuts : un incident CLOSED n'est plus modifiable
- [x] Un compte désactivé ne peut plus se connecter, et ses JWT encore valides sont refusés
- [x] Validation des données (`@NotBlank`, `@Email`, `@Size`, `@NotNull`, `@Valid`)
- [x] Erreurs JSON homogènes (`@RestControllerAdvice`) : 400, 401, 403, 404, 409
- [x] Historique des incidents (`IncidentHistory`) et notes de traitement (`IncidentComment`)
- [x] Dashboard adapté au rôle (EMPLOYEE, TECHNICIAN, ADMIN)
- [x] Gestion du profil et changement de mot de passe (vérification de l'ancien)
- [x] React : `ProtectedRoute`, `RoleRoute`, intercepteurs Axios (ajout du Bearer, gestion du 401, refresh automatique, redirection vers `/login` avec le message « session expirée »)
- [x] 9 tests JUnit/MockMvc et une collection Postman de 53 requêtes et 94 assertions, toutes au vert

## 9. Captures d'écran

Toutes les captures proviennent d'exécutions réelles de l'application (frontend dans le navigateur, backend sur le port 8085). Cliquez sur une section pour la déplier.

<details open>
<summary><b>🔐 Pages publiques (connexion, inscription)</b></summary>

| Connexion | Inscription : validation |
|:---:|:---:|
| <img src="rapport/captures/A01_login.png" alt="Connexion"> | <img src="rapport/captures/A02_register_validation.png" alt="Inscription : validation"> |
| **Email déjà utilisé (409)** | **Mauvais mot de passe** |
| <img src="rapport/captures/A03_register_email_existant.png" alt="Email déjà utilisé (409)"> | <img src="rapport/captures/A04_login_erreur.png" alt="Mauvais mot de passe"> |
| **Compte désactivé** | |
| <img src="rapport/captures/A05_login_compte_desactive.png" alt="Compte désactivé"> |  |

</details>
<details>
<summary><b>👤 Employé (EMPLOYEE)</b></summary>

| Dashboard | Mes incidents |
|:---:|:---:|
| <img src="rapport/captures/B01_employee_dashboard.png" alt="Dashboard"> | <img src="rapport/captures/B02_employee_mes_incidents.png" alt="Mes incidents"> |
| **Nouvel incident : validation** | **Nouvel incident** |
| <img src="rapport/captures/B03_nouvel_incident_validation.png" alt="Nouvel incident : validation"> | <img src="rapport/captures/B04_nouvel_incident_formulaire.png" alt="Nouvel incident"> |
| **Incident créé** | **Détail et historique** |
| <img src="rapport/captures/B05_incident_cree_details.png" alt="Incident créé"> | <img src="rapport/captures/B06_employee_detail_historique.png" alt="Détail et historique"> |
| **Incident d'un autre (403)** | **Route ADMIN interdite** |
| <img src="rapport/captures/B07_employee_incident_autre_403.png" alt="Incident d'un autre (403)"> | <img src="rapport/captures/B08_employee_route_admin_forbidden.png" alt="Route ADMIN interdite"> |
| **Profil** | **État final et historique** |
| <img src="rapport/captures/B09_profil.png" alt="Profil"> | <img src="rapport/captures/B10_employee_etat_final_historique.png" alt="État final et historique"> |

</details>
<details>
<summary><b>🛠️ Technicien (TECHNICIAN)</b></summary>

| Dashboard | Incidents affectés |
|:---:|:---:|
| <img src="rapport/captures/D01_technicien_dashboard.png" alt="Dashboard"> | <img src="rapport/captures/D02_technicien_incidents_affectes.png" alt="Incidents affectés"> |
| **Traitement et notes** | **Incident non affecté (403)** |
| <img src="rapport/captures/D03_technicien_traitement.png" alt="Traitement et notes"> | <img src="rapport/captures/D04_technicien_incident_non_affecte.png" alt="Incident non affecté (403)"> |

</details>
<details>
<summary><b>👑 Administrateur (ADMIN)</b></summary>

| Dashboard | Tous les incidents |
|:---:|:---:|
| <img src="rapport/captures/C01_admin_dashboard.png" alt="Dashboard"> | <img src="rapport/captures/C02_admin_tous_incidents.png" alt="Tous les incidents"> |
| **Gestion des utilisateurs** | **Affectation à un technicien** |
| <img src="rapport/captures/C03_admin_utilisateurs.png" alt="Gestion des utilisateurs"> | <img src="rapport/captures/C04_admin_affectation.png" alt="Affectation à un technicien"> |
| **Session expirée** | **Console H2 : mots de passe BCrypt** |
| <img src="rapport/captures/E01_session_expiree.png" alt="Session expirée"> | <img src="rapport/captures/E02_h2_bcrypt.png" alt="Console H2 : mots de passe BCrypt"> |

</details>
<details open>
<summary><b>📮 Tests Postman (9 tests de sécurité obligatoires)</b></summary>

| Collection Runner : 47/47 | Collection Runner (suite) |
|:---:|:---:|
| <img src="rapport/captures/postman/V11_runner_1.png" alt="Collection Runner : 47/47"> | <img src="rapport/captures/postman/V12_runner_2.png" alt="Collection Runner (suite)"> |
| **Test 1 : sans JWT → 401** | **Test 2 : JWT invalide → 401** |
| <img src="rapport/captures/postman/V01_S1_sans_jwt.png" alt="Test 1 : sans JWT → 401"> | <img src="rapport/captures/postman/V02_S2_jwt_invalide.png" alt="Test 2 : JWT invalide → 401"> |
| **Test 3 : JWT expiré → 401** | **Test 4 : EMPLOYEE → route ADMIN 403** |
| <img src="rapport/captures/postman/V03_S3_jwt_expire.png" alt="Test 3 : JWT expiré → 401"> | <img src="rapport/captures/postman/V04_S4_employee_admin.png" alt="Test 4 : EMPLOYEE → route ADMIN 403"> |
| **Test 5 : incident d'un autre → 403** | **Test 6 : technicien non affecté → 403** |
| <img src="rapport/captures/postman/V05_S5_incident_autre.png" alt="Test 5 : incident d'un autre → 403"> | <img src="rapport/captures/postman/V06_S6_technicien.png" alt="Test 6 : technicien non affecté → 403"> |
| **Test 7 : compte désactivé → 403** | **Test 8 : email existant → 409** |
| <img src="rapport/captures/postman/V07_S7_desactive.png" alt="Test 7 : compte désactivé → 403"> | <img src="rapport/captures/postman/V08_S8_email_existant.png" alt="Test 8 : email existant → 409"> |
| **Test 9 : données invalides → 400** | **Bonus : role=ADMIN ignoré** |
| <img src="rapport/captures/postman/V09_S9_donnees_invalides.png" alt="Test 9 : données invalides → 400"> | <img src="rapport/captures/postman/V10_S10_role_admin_ignore.png" alt="Bonus : role=ADMIN ignoré"> |

</details>
<details>
<summary><b>💻 Tests en ligne de commande (curl)</b></summary>

| Claims du JWT | JWT réellement expiré |
|:---:|:---:|
| <img src="rapport/captures/T08_jwt_claims.png" alt="Claims du JWT"> | <img src="rapport/captures/S3_jwt_expire.png" alt="JWT réellement expiré"> |
| **JWT falsifié (rôle ADMIN injecté)** | **Utilisateur désactivé** |
| <img src="rapport/captures/S2_jwt_invalide.png" alt="JWT falsifié (rôle ADMIN injecté)"> | <img src="rapport/captures/S7_utilisateur_desactive.png" alt="Utilisateur désactivé"> |
| **Incident CLOSED non modifiable** | **Refresh token et rotation** |
| <img src="rapport/captures/E3_incident_closed.png" alt="Incident CLOSED non modifiable"> | <img src="rapport/captures/E5_refresh_logout.png" alt="Refresh token et rotation"> |
| **CORS** | **mvn test : 9/9** |
| <img src="rapport/captures/E9_cors.png" alt="CORS"> | <img src="rapport/captures/T01_mvn_test.png" alt="mvn test : 9/9"> |

</details>
<details>
<summary><b>✅ Exécution Newman (collection Postman en ligne de commande)</b></summary>

| Scénario (1/2) | Scénario (2/2) |
|:---:|:---:|
| <img src="rapport/captures/N1_newman_scenario_1.png" alt="Scénario (1/2)"> | <img src="rapport/captures/N2_newman_scenario_2.png" alt="Scénario (2/2)"> |
| **Sécurité (1/2)** | **Sécurité (2/2)** |
| <img src="rapport/captures/N3_newman_securite_1.png" alt="Sécurité (1/2)"> | <img src="rapport/captures/N4_newman_securite_2.png" alt="Sécurité (2/2)"> |
| **Résumé : 94 assertions, 0 échec** | |
| <img src="rapport/captures/N5_newman_resume.png" alt="Résumé : 94 assertions, 0 échec"> |  |

</details>

## 10. Rapport

Le rapport complet (analyse, architecture, travaux de recherche, réalisation étape par étape, tests et captures) : [`rapport/Rapport_Projet_JWT_Spring_Security_React.docx`](rapport/Rapport_Projet_JWT_Spring_Security_React.docx)
