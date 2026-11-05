<p align="center">
  <img src="rapport/logo_enset.png" alt="ENSET Mohammedia" height="80">
</p>

<h1 align="center">🛡️ Guardian Incidents</h1>

<p align="center">
  <b>Rapport de projet : authentification et autorisation avec Spring Security, JWT et React</b><br>
  Plateforme de déclaration et de suivi des incidents informatiques et de cybersécurité
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Security-6-6DB33F?logo=springsecurity&logoColor=white" alt="Spring Security">
  <img src="https://img.shields.io/badge/JWT-HS256-black?logo=jsonwebtokens" alt="JWT">
  <img src="https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black" alt="React">
  <img src="https://img.shields.io/badge/Vite-6-646CFF?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/Postman-94%20assertions%20OK-FF6C37?logo=postman&logoColor=white" alt="Postman">
  <img src="https://img.shields.io/badge/JUnit-9%2F9%20OK-25A162?logo=junit5&logoColor=white" alt="JUnit">
</p>

| | |
|---|---|
| **Établissement** | École Normale Supérieure de l'Enseignement Technique de Mohammedia (ENSET), Université Hassan II de Casablanca |
| **Filière / module** | SDIA, 2ème année · Sécurité des systèmes distribués |
| **Réalisé par** | **Hicham OUAOUCHE** |
| **Encadré par** | **Pr. Abdelmajid BOUSSELHAM** |
| **Année universitaire** | 2026 – 2027 |
| **Rapport PDF** | 📄 [Hicham_OUAOUCHE_JWT_Spring_Security_React.pdf](rapport/Hicham_OUAOUCHE_JWT_Spring_Security_React.pdf) |

---

## Table des matières

1. [Introduction](#1-introduction)
2. [Analyse des besoins](#2-analyse-des-besoins)
3. [Architecture générale](#3-architecture-générale)
4. [Modèle de données](#4-modèle-de-données)
5. [Réalisation du backend](#5-réalisation-du-backend)
6. [Réalisation du frontend React](#6-réalisation-du-frontend-react)
7. [Lancer le projet](#7-lancer-le-projet)
8. [Tests de sécurité (Postman et curl)](#8-tests-de-sécurité-postman-et-curl)
9. [Scénario de test et captures de l'application](#9-scénario-de-test-et-captures-de-lapplication)
10. [Endpoints de l'API](#10-endpoints-de-lapi)
11. [Bilan de sécurité](#11-bilan-de-sécurité)
12. [Conclusion](#12-conclusion)

---
## 1. Introduction

### 1.1 Contexte
Les entreprises font face chaque jour à des incidents informatiques et de cybersécurité : postes infectés par un malware, tentatives de phishing, comptes bloqués, pannes réseau, fuites potentielles de données, comportements suspects… **Guardian Incidents** est une application web où les employés **déclarent** ces incidents, où les techniciens les **traitent** et où l'administrateur **supervise** l'ensemble.

Le projet prolonge l'atelier JWT du cours : un backend **Spring Boot** sécurisé par **Spring Security** et des **JSON Web Tokens**, et un frontend **React** qui consomme l'API REST avec **Axios**.

### 1.2 Objectifs
- authentification **stateless** avec JWT ;
- gestion de **trois rôles** (EMPLOYEE, TECHNICIAN, ADMIN) ;
- protection des **routes backend** (règles d'URL et `@PreAuthorize`) et des **routes React** (`ProtectedRoute`, `RoleRoute`) ;
- **contrôle d'accès aux données** : un employé ne voit que ses incidents, un technicien que ceux qui lui sont affectés ;
- validation des données, gestion propre des erreurs, tableau de bord par rôle, historique et notes de traitement.

### 1.3 Technologies

| Couche | Technologies |
|--------|--------------|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA (Hibernate 6.4), Bean Validation, JJWT 0.12.6, Lombok |
| Base de données | H2 en mémoire (par défaut), MySQL (profil `mysql`) |
| Frontend | React 18, Vite 6, React Router 6, Axios |
| Tests | JUnit 5 + MockMvc, Postman (Collection Runner), Newman, curl |

---

## 2. Analyse des besoins

### 2.1 Rôles

| Rôle | Description | Création du compte |
|------|-------------|--------------------|
| **EMPLOYEE** | Déclare des incidents et consulte uniquement les siens | Inscription publique `/auth/register` (rôle imposé) |
| **TECHNICIAN** | Traite les incidents qui lui sont affectés (statut, notes) | Créé au démarrage (`DataInitializer`) |
| **ADMIN** | Gère les utilisateurs, affecte les incidents, consulte les statistiques | Créé au démarrage (`DataInitializer`) |

### 2.2 Matrice des droits

| Fonctionnalité | EMPLOYEE | TECHNICIAN | ADMIN |
|---|:---:|:---:|:---:|
| Consulter / modifier son profil, changer son mot de passe | ✅ | ✅ | ✅ |
| Créer un incident | ✅ | ❌ | ❌ |
| Consulter le détail d'un incident | si propriétaire | si affecté | ✅ tous |
| Consulter les incidents affectés | ❌ | ✅ | – |
| Modifier le statut | ❌ | si affecté (IN_PROGRESS, RESOLVED) | ✅ (y compris CLOSED) |
| Ajouter une note de traitement | ❌ | si affecté | ✅ |
| Affecter un incident à un technicien | ❌ | ❌ | ✅ |
| Lister / activer / désactiver les utilisateurs | ❌ | ❌ | ✅ |
| Statistiques globales | ❌ | ❌ | ✅ |

### 2.3 Règles métier
1. Un nouvel utilisateur reçoit automatiquement le rôle **EMPLOYEE**.
2. Un utilisateur ne peut jamais choisir le rôle ADMIN lors de son inscription.
3. Les mots de passe sont encodés avec **BCrypt**.
4. Un employé ne peut consulter que ses propres incidents.
5. Un technicien ne peut modifier que les incidents qui lui sont affectés.
6. Seul un administrateur peut affecter un technicien.
7. Un utilisateur désactivé ne peut plus se connecter.
8. Un incident **CLOSED** ne peut plus changer de statut.

> Toutes ces règles sont appliquées **côté serveur**. Le frontend adapte seulement l'affichage : masquer un bouton n'est jamais une protection.

---

## 3. Architecture générale

Deux projets indépendants : un frontend React (port **5173**) et une API REST Spring Boot (port **8085**). Ils communiquent en HTTP/JSON, et chaque requête protégée porte l'en-tête `Authorization: Bearer <JWT>`.

<p align="center"><img src="rapport/diagrammes/architecture.png" alt="Architecture générale de l'application" width="85%"><br><i>Figure 1 : Architecture générale de l'application</i></p>

> Le port **8085** remplace 8080, déjà occupé sur la machine de développement. Il se modifie dans `application.properties` (`server.port`) et `frontend/.env` (`VITE_API_URL`).

### Arborescence du projet

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
│       │   ├── exception/       # Exceptions métier + GlobalExceptionHandler
│       │   ├── repository/      # Interfaces Spring Data JPA
│       │   ├── security/        # JwtService, JwtAuthenticationFilter, EntryPoint 401, AccessDenied 403
│       │   └── service/         # Logique métier, contrôle propriétaire / affectation
│       ├── main/resources/      # application.properties, application-mysql.properties
│       └── test/java/com/sdia/incidents/SecurityIntegrationTest.java
├── frontend/                                 # Projet React (Vite)
│   └── src/
│       ├── components/      # Navbar, ProtectedRoute, RoleRoute, Badge, IncidentTable
│       ├── context/         # AuthContext
│       ├── pages/           # Login, Register, Dashboard, Profile, MyIncidents, NewIncident,
│       │                    # IncidentDetails, AssignedIncidents, AdminUsers, AdminIncidents, ...
│       ├── services/        # api.js (intercepteurs Axios), authService, incidentService, userService
│       └── App.jsx, main.jsx, index.css
└── rapport/
    ├── Hicham_OUAOUCHE_JWT_Spring_Security_React.pdf
    ├── logo_enset.png
    ├── diagrammes/          # architecture, entités, flux JWT
    └── captures/            # captures de l'application et du terminal
        └── postman/         # captures des tests Postman
```

---

## 4. Modèle de données

<p align="center"><img src="rapport/diagrammes/entites.png" alt="Diagramme des entités JPA" width="85%"><br><i>Figure 2 : Diagramme des entités JPA</i></p>

| Entité | Attributs principaux | Rôle |
|--------|---------------------|------|
| `User` | id, fullName, email (unique), password (BCrypt), role, enabled, createdAt | Compte ; implémente `UserDetails` |
| `Incident` | id, title, description, category, priority, status, createdAt, updatedAt, createdBy, assignedTo | Incident déclaré |
| `IncidentHistory` | id, incident, action, description, oldStatus, newStatus, performedBy, createdAt | Trace des changements importants |
| `IncidentComment` | id, incident, author, content, createdAt | Notes de traitement |
| `RefreshToken` | id, token (UUID), user, expiryDate, revoked | Jeton de renouvellement révocable |

**Énumérations** (stockées en `EnumType.STRING`) : `Role` (EMPLOYEE, TECHNICIAN, ADMIN), `Category` (NETWORK, MALWARE, PHISHING, ACCESS, SOFTWARE, HARDWARE, DATA_LEAK, OTHER), `Priority` (LOW, MEDIUM, HIGH, CRITICAL), `Status` (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED).

### Travail de recherche : relations JPA, LAZY et EAGER

| Stratégie | Fonctionnement | Défaut JPA |
|-----------|----------------|-----------|
| **EAGER** | L'association est chargée immédiatement avec l'entité | `@ManyToOne`, `@OneToOne` |
| **LAZY** | Un proxy est placé ; la donnée est chargée au premier accès | `@OneToMany`, `@ManyToMany` |

**Choix effectués :**
- `Incident.createdBy` : `@ManyToOne(fetch = LAZY, optional = false)`. Un utilisateur crée plusieurs incidents ; LAZY évite de charger l'utilisateur à chaque fois.
- `Incident.assignedTo` : `@ManyToOne(fetch = LAZY)`, nullable. Un incident est « éventuellement » affecté (0..1).
- `Incident.history` et `Incident.comments` : `@OneToMany(mappedBy = "incident", cascade = ALL, orphanRemoval = true)` avec `@OrderBy("createdAt ASC")`. Leur cycle de vie est lié à l'incident.
- Pas de `@OneToMany` côté `User` : on passe par des requêtes ciblées du repository.
- `@EntityGraph(attributePaths = {"createdBy", "assignedTo"})` dans les listes pour éviter le problème N+1, et `open-in-view=false`.

**`entity/Incident.java (relations)`**

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "created_by_id", nullable = false)
private User createdBy;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "assigned_to_id")
private User assignedTo;

@OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("createdAt ASC")
@Builder.Default
private List<IncidentHistory> history = new ArrayList<>();

@OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("createdAt ASC")
@Builder.Default
private List<IncidentComment> comments = new ArrayList<>();
```

---

## 5. Réalisation du backend

### 5.1 Validation des données (`@NotBlank`, `@Email`, `@Size`, `@Valid`)
Le DTO d'inscription ne contient **aucun champ `role`** : un `"role":"ADMIN"` envoyé par un client est simplement ignoré (règles 1 et 2).

**`dto/RegisterRequest.java`**

```java
public record RegisterRequest(
        @NotBlank(message = "Le nom complet est obligatoire")
        @Size(min = 3, max = 100, message = "Le nom complet doit contenir entre 3 et 100 caracteres")
        String fullName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caracteres")
        String password
) {}
```

### 5.2 Le JWT et ses claims
Un JWT est composé de trois parties en Base64URL : **header.payload.signature**. La signature HMAC-SHA256 (clé de 256 bits) garantit qu'aucun claim ne peut être modifié.

| Claim | Type | Contenu |
|-------|------|---------|
| `sub` | enregistré | email de l'utilisateur |
| `iat` | enregistré | date de création |
| `exp` | enregistré | date d'expiration (**+5 minutes**) |
| `role` | privé | rôle de l'utilisateur |
| `fullName` | privé | nom complet |

**`security/JwtService.java`**

```java
public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessExpirationMs);
    return Jwts.builder()
            .subject(user.getEmail())
            .claim(ROLE_CLAIM, user.getRole().name())
            .claim("fullName", user.getFullName())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
}
```
<p align="center"><img src="rapport/captures/T08_jwt_claims.png" alt="Décodage réel d'un JWT émis par l'API : sub, role, iat, exp (5 minutes)"><br><i>Figure 3 : Décodage réel d'un JWT émis par l'API : sub, role, iat, exp (5 minutes)</i></p>

### 5.3 Le filtre `JwtAuthenticationFilter`
Pour chaque requête : lecture de l'en-tête `Bearer`, vérification de la signature et de l'expiration, **rechargement de l'utilisateur en base** (un compte désactivé est refusé immédiatement), puis remplissage du `SecurityContextHolder`. En cas d'erreur, la raison est transmise au point d'entrée 401 (« Token expire », « Token invalide », « Compte desactive »).

### 5.4 Configuration de Spring Security
**`config/SecurityConfig.java (extrait)`**

```java
.authorizeHttpRequests(auth -> auth
        // ---- Routes publiques ----
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        .requestMatchers("/error").permitAll()
        // ---- Routes reservees a l'ADMIN (1er niveau : URL) ----
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        // ---- Toutes les autres routes /api/** : authentifie ----
        // (le controle fin par role est fait par @PreAuthorize)
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll()
)
.exceptionHandling(ex -> ex
        .authenticationEntryPoint(authenticationEntryPoint)   // 401
        .accessDeniedHandler(accessDeniedHandler))            // 403
.authenticationProvider(authenticationProvider())
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

- **Routes publiques** : `/auth/**`, `/h2-console/**` ; **routes protégées** : `/api/**` ; `/api/admin/**` réservé à ADMIN.
- `SessionCreationPolicy.STATELESS`, CSRF désactivé (pas de cookie de session), CORS limité à `http://localhost:5173`, `BCryptPasswordEncoder`.

### 5.5 Sécurisation selon les rôles (`hasRole`, `@PreAuthorize`, `@EnableMethodSecurity`)
La sécurité est appliquée sur **deux niveaux** : les règles d'URL et `@PreAuthorize` sur chaque endpoint (activé par `@EnableMethodSecurity`). `hasRole('ADMIN')` vérifie l'autorité `ROLE_ADMIN` ; `hasAuthority` vérifie l'autorité exacte, sans préfixe.

**`controller/IncidentController.java (extrait)`**

```java
@PutMapping("/{id}/status")
@PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
public IncidentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
    return incidentService.updateStatus(id, request);
}

@PutMapping("/{id}/assign/{technicianId}")
@PreAuthorize("hasRole('ADMIN')")
public IncidentResponse assign(@PathVariable Long id, @PathVariable Long technicianId) {
    return incidentService.assign(id, technicianId);
}
```

### 5.6 Contrôle du propriétaire d'un incident
Un employé peut appeler `GET /api/incidents/12` directement avec Postman. Le backend récupère l'utilisateur connecté via le `SecurityContextHolder` (`CurrentUserService`) et le compare au propriétaire ou au technicien affecté **avant** de renvoyer les données.

**`service/IncidentService.java`**

```java
private void checkReadAccess(Incident incident, User me) {
    boolean allowed = switch (me.getRole()) {
        case ADMIN -> true;
        case EMPLOYEE -> incident.getCreatedBy().getId().equals(me.getId());
        case TECHNICIAN -> isAssignedTo(incident, me);
    };
    if (!allowed) {
        throw new ForbiddenOperationException(
                "Acces interdit : vous n'avez pas le droit de consulter cet incident");
    }
}
```

### 5.7 Machine à états des statuts
`CLOSED` est un état final (règle 8). L'affectation par l'admin fait passer l'incident de OPEN à ASSIGNED ; le technicien le passe à IN_PROGRESS puis RESOLVED ; l'admin le clôture.

**`service/IncidentService.java (transitions)`**

```java
private static final Map<Status, Set<Status>> TRANSITIONS = Map.of(
        Status.OPEN,        EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
        Status.ASSIGNED,    EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
        Status.IN_PROGRESS, EnumSet.of(Status.RESOLVED, Status.CLOSED),
        Status.RESOLVED,    EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
        Status.CLOSED,      EnumSet.noneOf(Status.class)
);
```

### 5.8 Gestion des erreurs (`@RestControllerAdvice`)

| Code | Signification | Exemples |
|------|---------------|----------|
| **400** Bad Request | Données invalides | échec `@Valid`, enum inconnue, ancien mot de passe faux |
| **401** Unauthorized | Non authentifié | JWT absent, invalide ou expiré, mauvais mot de passe |
| **403** Forbidden | Authentifié mais pas autorisé | route ADMIN, incident d'un autre, compte désactivé |
| **404** Not Found | Ressource inexistante | incident introuvable |
| **409** Conflict | Conflit avec l'état actuel | email déjà utilisé, incident CLOSED |

```json
{ "status": 404, "error": "Not Found", "message": "Incident introuvable", "path": "/api/incidents/9999", "timestamp": "..." }
```

### 5.9 Historique, notes et refresh token
- **Historique** (`IncidentHistory`) : création, affectation et chaque changement de statut, avec l'auteur et la date.
- **Notes de traitement** (`IncidentComment`) : ajoutées par le technicien affecté, visibles par l'employé propriétaire.
- **Refresh token** : UUID stocké en base, valable 24 h, avec **rotation** à chaque renouvellement (`POST /auth/refresh`) et **révocation** à la déconnexion (`POST /auth/logout`).

| | Access token (JWT) | Refresh token |
|---|---|---|
| Rôle | Prouver l'identité à chaque requête | Obtenir un nouvel access token |
| Durée de vie | 5 minutes | 24 heures |
| Révocation | Impossible avant `exp` | Champ `revoked` en base |

---

## 6. Réalisation du frontend React

### 6.1 Gestion du JWT avec Axios
Après la connexion, le JWT, le refresh token et le rôle sont stockés dans le `localStorage`. L'intercepteur de **requête** ajoute automatiquement `Authorization: Bearer <JWT>` :

**`services/api.js`**

```javascript
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token && !config.url.startsWith('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

L'intercepteur de **réponse** (`axios.interceptors.response.use`) gère le **401** : il tente un `POST /auth/refresh` puis rejoue la requête ; si c'est impossible, il **supprime le JWT**, **redirige vers `/login`** et affiche **« Votre session a expiré »**.

### 6.2 Protection des routes
**`components/RoleRoute.jsx`**

```jsx
export default function RoleRoute({ roles }) {
  const user = getUser()
  if (!user) return <Navigate to="/login" replace />
  if (!roles.includes(user.role)) return <Navigate to="/forbidden" replace />
  return <Outlet />
}
```

`ProtectedRoute` redirige un visiteur non connecté vers `/login` ; `RoleRoute` redirige vers la page 403 si le rôle ne convient pas (ex. `/admin/users` réservé à ADMIN). La vraie sécurité reste dans le backend.

| Page | Route | Accès |
|------|-------|-------|
| Login / Register | `/login`, `/register` | Public |
| Dashboard / Profile | `/dashboard`, `/profile` | Connecté |
| MyIncidents / NewIncident | `/incidents/my`, `/incidents/new` | EMPLOYEE |
| IncidentDetails | `/incidents/:id` | Connecté (contrôle serveur) |
| AssignedIncidents | `/incidents/assigned` | TECHNICIAN |
| AdminIncidents / AdminUsers | `/admin/incidents`, `/admin/users` | ADMIN |

---

## 7. Lancer le projet

**Prérequis** : Java 21, Maven 3.9+, Node.js 18+.

```bash
# Backend (port 8085)
cd backend
mvn spring-boot:run            # MySQL : mvn spring-boot:run -Dspring-boot.run.profiles=mysql

# Frontend (port 5173)
cd frontend
npm install
npm run dev                    # puis ouvrir http://localhost:5173

# Tests d'intégration
cd backend && mvn test
```

<p align="center"><img src="rapport/captures/T02_backend_run.png" alt="Démarrage du backend : port 8085, chaîne de filtres et comptes initiaux"><br><i>Figure 4 : Démarrage du backend : port 8085, chaîne de filtres et comptes initiaux</i></p>
<p align="center"><img src="rapport/captures/T03_frontend_dev.png" alt="Démarrage du frontend Vite sur le port 5173"><br><i>Figure 5 : Démarrage du frontend Vite sur le port 5173</i></p>

| Service | URL |
|---------|-----|
| API Spring Boot | http://localhost:8085 |
| Console H2 | http://localhost:8085/h2-console (JDBC `jdbc:h2:mem:incidentsdb`, user `sa`, sans mot de passe) |
| Application React | http://localhost:5173 |

### Comptes de test

| Rôle | Nom | Email | Mot de passe |
|------|-----|-------|--------------|
| ADMIN | Hicham Ouaouche | hichamouaouche@gmail.com | `Admin@2026` |
| TECHNICIAN | Youssef Amrani | tech1@incidents.ma | `Tech@2026` |
| TECHNICIAN | Karim Idrissi | tech2@incidents.ma | `Tech@2026` |
| EMPLOYEE | à créer via `/register` | ex. ahmed.benali@entreprise.ma | ex. `Password123` |

Les mots de passe sont stockés hachés avec BCrypt :

<p align="center"><img src="rapport/captures/E02_h2_bcrypt.png" alt="Console H2 : table USERS, mots de passe hachés avec BCrypt"><br><i>Figure 6 : Console H2 : table USERS, mots de passe hachés avec BCrypt</i></p>

---

## 8. Tests de sécurité (Postman et curl)

### 8.1 Tests d'intégration JUnit / MockMvc
<p align="center"><img src="rapport/captures/T01_mvn_test.png" alt="mvn test : 9 tests, 0 échec, BUILD SUCCESS"><br><i>Figure 7 : mvn test : 9 tests, 0 échec, BUILD SUCCESS</i></p>

### 8.2 Postman : exécution du scénario (Collection Runner)
<p align="center"><img src="rapport/captures/postman/V11_runner_1.png" alt="Postman Collection Runner : scénario de test, 47 assertions réussies, 0 échec"><br><i>Figure 8 : Postman Collection Runner : scénario de test, 47 assertions réussies, 0 échec</i></p>
<p align="center"><img src="rapport/captures/postman/V12_runner_2.png" alt="Postman Collection Runner : suite du scénario"><br><i>Figure 9 : Postman Collection Runner : suite du scénario</i></p>
<p align="center"><img src="rapport/captures/postman/V00_inscription.png" alt="Postman : inscription d'un employé, 201 Created, rôle EMPLOYEE, mot de passe absent de la réponse"><br><i>Figure 10 : Postman : inscription d'un employé, 201 Created, rôle EMPLOYEE, mot de passe absent de la réponse</i></p>

### 8.3 Les 9 tests de sécurité obligatoires

| N° | Situation testée | Attendu | Obtenu |
|:--:|------------------|:-------:|--------|
| 1 | API protégée sans JWT | 401 | ✅ 401 « token JWT manquant » |
| 2 | JWT invalide | 401 | ✅ 401 « Token invalide » |
| 3 | JWT expiré | 401 | ✅ 401 « Token expire » |
| 4 | EMPLOYEE sur une route ADMIN | 403 | ✅ 403 |
| 5 | EMPLOYEE sur l'incident d'un autre | 403 | ✅ 403 |
| 6 | TECHNICIAN sur un incident non affecté | 403 | ✅ 403 |
| 7 | Utilisateur désactivé qui se connecte | 403 | ✅ 403 « Compte desactive » |
| 8 | Inscription avec un email existant | 409 | ✅ 409 |
| 9 | Incident avec des données invalides | 400 | ✅ 400 + détail des erreurs |

#### Test 1 : accès à une API protégée sans JWT

<p align="center"><img src="rapport/captures/postman/V01_S1_sans_jwt.png" alt="Postman : accès à une API protégée sans JWT → 401"><br><i>Figure 11 : Postman : accès à une API protégée sans JWT → 401</i></p>
<p align="center"><img src="rapport/captures/S1_sans_jwt.png" alt="curl : accès à une API protégée sans JWT → 401"><br><i>Figure 12 : curl : accès à une API protégée sans JWT → 401</i></p>
#### Test 2 : JWT invalide (et JWT falsifié avec le rôle ADMIN)

<p align="center"><img src="rapport/captures/postman/V02_S2_jwt_invalide.png" alt="Postman : JWT invalide (et JWT falsifié avec le rôle ADMIN) → 401 Token invalide"><br><i>Figure 13 : Postman : JWT invalide (et JWT falsifié avec le rôle ADMIN) → 401 Token invalide</i></p>
<p align="center"><img src="rapport/captures/S2_jwt_invalide.png" alt="curl : JWT invalide (et JWT falsifié avec le rôle ADMIN) → 401 Token invalide"><br><i>Figure 14 : curl : JWT invalide (et JWT falsifié avec le rôle ADMIN) → 401 Token invalide</i></p>
#### Test 3 : JWT expiré

<p align="center"><img src="rapport/captures/postman/V03_S3_jwt_expire.png" alt="Postman : JWT expiré → 401 Token expire"><br><i>Figure 15 : Postman : JWT expiré → 401 Token expire</i></p>
<p align="center"><img src="rapport/captures/S3_jwt_expire.png" alt="curl : JWT expiré → 401 Token expire"><br><i>Figure 16 : curl : JWT expiré → 401 Token expire</i></p>
#### Test 4 : EMPLOYEE sur une route ADMIN

<p align="center"><img src="rapport/captures/postman/V04_S4_employee_admin.png" alt="Postman : EMPLOYEE sur une route ADMIN → 403"><br><i>Figure 17 : Postman : EMPLOYEE sur une route ADMIN → 403</i></p>
<p align="center"><img src="rapport/captures/S4_employee_route_admin.png" alt="curl : EMPLOYEE sur une route ADMIN → 403"><br><i>Figure 18 : curl : EMPLOYEE sur une route ADMIN → 403</i></p>
#### Test 5 : EMPLOYEE sur l'incident d'un autre utilisateur

<p align="center"><img src="rapport/captures/postman/V05_S5_incident_autre.png" alt="Postman : EMPLOYEE sur l'incident d'un autre utilisateur → 403"><br><i>Figure 19 : Postman : EMPLOYEE sur l'incident d'un autre utilisateur → 403</i></p>
<p align="center"><img src="rapport/captures/S5_employee_incident_autre.png" alt="curl : EMPLOYEE sur l'incident d'un autre utilisateur → 403"><br><i>Figure 20 : curl : EMPLOYEE sur l'incident d'un autre utilisateur → 403</i></p>
#### Test 6 : TECHNICIAN sur un incident non affecté

<p align="center"><img src="rapport/captures/postman/V06_S6_technicien.png" alt="Postman : TECHNICIAN sur un incident non affecté → 403"><br><i>Figure 21 : Postman : TECHNICIAN sur un incident non affecté → 403</i></p>
<p align="center"><img src="rapport/captures/S6_technicien_non_affecte.png" alt="curl : TECHNICIAN sur un incident non affecté → 403"><br><i>Figure 22 : curl : TECHNICIAN sur un incident non affecté → 403</i></p>
#### Test 7 : utilisateur désactivé

<p align="center"><img src="rapport/captures/postman/V07_S7_desactive.png" alt="Postman : utilisateur désactivé → 403 Compte desactive"><br><i>Figure 23 : Postman : utilisateur désactivé → 403 Compte desactive</i></p>
<p align="center"><img src="rapport/captures/S7_utilisateur_desactive.png" alt="curl : utilisateur désactivé → 403 Compte desactive"><br><i>Figure 24 : curl : utilisateur désactivé → 403 Compte desactive</i></p>
#### Test 8 : inscription avec un email déjà existant

<p align="center"><img src="rapport/captures/postman/V08_S8_email_existant.png" alt="Postman : inscription avec un email déjà existant → 409"><br><i>Figure 25 : Postman : inscription avec un email déjà existant → 409</i></p>
<p align="center"><img src="rapport/captures/S8_email_existant.png" alt="curl : inscription avec un email déjà existant → 409"><br><i>Figure 26 : curl : inscription avec un email déjà existant → 409</i></p>
#### Test 9 : création d'un incident avec des données invalides

<p align="center"><img src="rapport/captures/postman/V09_S9_donnees_invalides.png" alt="Postman : création d'un incident avec des données invalides → 400"><br><i>Figure 27 : Postman : création d'un incident avec des données invalides → 400</i></p>
<p align="center"><img src="rapport/captures/S9_incident_invalide.png" alt="curl : création d'un incident avec des données invalides → 400"><br><i>Figure 28 : curl : création d'un incident avec des données invalides → 400</i></p>
<p align="center"><img src="rapport/captures/S6a_admin_affecte.png" alt="Préparation du test 6 : l'ADMIN affecte l'incident 1 au technicien Youssef Amrani"><br><i>Figure 29 : Préparation du test 6 : l'ADMIN affecte l'incident 1 au technicien Youssef Amrani</i></p>
<p align="center"><img src="rapport/captures/postman/V10_S10_role_admin_ignore.png" alt="Test complémentaire : inscription avec "role":"ADMIN", le rôle reste EMPLOYEE"><br><i>Figure 30 : Test complémentaire : inscription avec "role":"ADMIN", le rôle reste EMPLOYEE</i></p>

### 8.4 Tests complémentaires (curl)
<p align="center"><img src="rapport/captures/T04_api_sh.png" alt="Script utilitaire api.sh utilisé pour les tests curl"><br><i>Figure 31 : Script utilitaire api.sh utilisé pour les tests curl</i></p>
<p align="center"><img src="rapport/captures/T05_register.png" alt="Inscription de deux employés : 201, rôle EMPLOYEE"><br><i>Figure 32 : Inscription de deux employés : 201, rôle EMPLOYEE</i></p>
<p align="center"><img src="rapport/captures/T06_register_role_admin.png" alt="Inscription avec role=ADMIN : ignoré"><br><i>Figure 33 : Inscription avec role=ADMIN : ignoré</i></p>
<p align="center"><img src="rapport/captures/T07_login.png" alt="Connexion : token, refreshToken, role, fullName"><br><i>Figure 34 : Connexion : token, refreshToken, role, fullName</i></p>
<p align="center"><img src="rapport/captures/T09_create_incident.png" alt="Création d'un incident : statut OPEN"><br><i>Figure 35 : Création d'un incident : statut OPEN</i></p>
<p align="center"><img src="rapport/captures/T10_my_incidents.png" alt="L'employé ne voit que ses propres incidents"><br><i>Figure 36 : L'employé ne voit que ses propres incidents</i></p>
<p align="center"><img src="rapport/captures/E1_traitement_technicien.png" alt="Traitement par le technicien : note et passage à RESOLVED"><br><i>Figure 37 : Traitement par le technicien : note et passage à RESOLVED</i></p>
<p align="center"><img src="rapport/captures/E2_historique_employe.png" alt="L'employé consulte l'état final, l'historique et les notes"><br><i>Figure 38 : L'employé consulte l'état final, l'historique et les notes</i></p>
<p align="center"><img src="rapport/captures/E3_incident_closed.png" alt="Règle 8 : un incident CLOSED n'est plus modifiable (409)"><br><i>Figure 39 : Règle 8 : un incident CLOSED n'est plus modifiable (409)</i></p>
<p align="center"><img src="rapport/captures/E4_404_mauvais_mdp.png" alt="Incident inexistant (404) et mauvais mot de passe (401)"><br><i>Figure 40 : Incident inexistant (404) et mauvais mot de passe (401)</i></p>
<p align="center"><img src="rapport/captures/E5_refresh_logout.png" alt="Refresh token : nouveau token puis rejet de l'ancien (rotation)"><br><i>Figure 41 : Refresh token : nouveau token puis rejet de l'ancien (rotation)</i></p>
<p align="center"><img src="rapport/captures/E6_logout.png" alt="Déconnexion : le refresh token révoqué est refusé"><br><i>Figure 42 : Déconnexion : le refresh token révoqué est refusé</i></p>
<p align="center"><img src="rapport/captures/E7_statistiques_dashboard.png" alt="Statistiques ADMIN et dashboard TECHNICIAN"><br><i>Figure 43 : Statistiques ADMIN et dashboard TECHNICIAN</i></p>
<p align="center"><img src="rapport/captures/E8_profil_mot_de_passe.png" alt="Changement de mot de passe : ancien mot de passe vérifié"><br><i>Figure 44 : Changement de mot de passe : ancien mot de passe vérifié</i></p>
<p align="center"><img src="rapport/captures/E9_cors.png" alt="CORS : origine localhost:5173 autorisée, origine inconnue refusée"><br><i>Figure 45 : CORS : origine localhost:5173 autorisée, origine inconnue refusée</i></p>

### 8.5 Collection Postman exécutée avec Newman
<p align="center"><img src="rapport/captures/N1_newman_scenario_1.png" alt="Newman : scénario de test (1/2)"><br><i>Figure 46 : Newman : scénario de test (1/2)</i></p>
<p align="center"><img src="rapport/captures/N2_newman_scenario_2.png" alt="Newman : scénario de test (2/2)"><br><i>Figure 47 : Newman : scénario de test (2/2)</i></p>
<p align="center"><img src="rapport/captures/N3_newman_securite_1.png" alt="Newman : tests de sécurité (1/2)"><br><i>Figure 48 : Newman : tests de sécurité (1/2)</i></p>
<p align="center"><img src="rapport/captures/N4_newman_securite_2.png" alt="Newman : tests de sécurité (2/2)"><br><i>Figure 49 : Newman : tests de sécurité (2/2)</i></p>
<p align="center"><img src="rapport/captures/N5_newman_resume.png" alt="Newman : 53 requêtes, 94 assertions, 0 échec"><br><i>Figure 50 : Newman : 53 requêtes, 94 assertions, 0 échec</i></p>

---

## 9. Scénario de test et captures de l'application

Captures réalisées dans le navigateur, sur l'application en fonctionnement (frontend `http://localhost:5173`, backend `http://localhost:8085`).

| Étape du scénario (section 24 du sujet) | Preuve |
|---|---|
| 1-3. Créer 2 EMPLOYEE, des TECHNICIAN et un ADMIN | Runner Postman, T05, T02 |
| 4-7. Les employés déclarent leurs incidents | B04, B05, T09 |
| 8. L'employé 1 ne peut pas lire l'incident de l'employé 2 | B07, test 5 |
| 9-11. L'ADMIN consulte tous les incidents et en affecte un | C02, C04 |
| 12-15. Le technicien traite l'incident et ajoute une note | D02, D03 |
| 16-17. L'employé vérifie l'état et l'historique | B10 |

### 9.1 Pages publiques
<p align="center"><img src="rapport/captures/A01_login.png" alt="Page de connexion"><br><i>Figure 51 : Page de connexion</i></p>
<p align="center"><img src="rapport/captures/A02_register_validation.png" alt="Inscription : erreurs de validation renvoyées par le backend"><br><i>Figure 52 : Inscription : erreurs de validation renvoyées par le backend</i></p>
<p align="center"><img src="rapport/captures/A03_register_email_existant.png" alt="Inscription avec un email existant (409)"><br><i>Figure 53 : Inscription avec un email existant (409)</i></p>
<p align="center"><img src="rapport/captures/A04_login_erreur.png" alt="Connexion avec un mauvais mot de passe"><br><i>Figure 54 : Connexion avec un mauvais mot de passe</i></p>
<p align="center"><img src="rapport/captures/A05_login_compte_desactive.png" alt="Connexion d'un compte désactivé"><br><i>Figure 55 : Connexion d'un compte désactivé</i></p>

### 9.2 Employé (EMPLOYEE)
<p align="center"><img src="rapport/captures/B01_employee_dashboard.png" alt="Dashboard EMPLOYEE : total, OPEN, IN_PROGRESS, RESOLVED/CLOSED"><br><i>Figure 56 : Dashboard EMPLOYEE : total, OPEN, IN_PROGRESS, RESOLVED/CLOSED</i></p>
<p align="center"><img src="rapport/captures/B02_employee_mes_incidents.png" alt="Mes incidents"><br><i>Figure 57 : Mes incidents</i></p>
<p align="center"><img src="rapport/captures/B03_nouvel_incident_validation.png" alt="Déclaration : validation des champs obligatoires"><br><i>Figure 58 : Déclaration : validation des champs obligatoires</i></p>
<p align="center"><img src="rapport/captures/B04_nouvel_incident_formulaire.png" alt="Formulaire de déclaration rempli"><br><i>Figure 59 : Formulaire de déclaration rempli</i></p>
<p align="center"><img src="rapport/captures/B05_incident_cree_details.png" alt="Incident créé : détail et historique"><br><i>Figure 60 : Incident créé : détail et historique</i></p>
<p align="center"><img src="rapport/captures/B06_employee_detail_historique.png" alt="Détail d'un incident en cours de traitement"><br><i>Figure 61 : Détail d'un incident en cours de traitement</i></p>
<p align="center"><img src="rapport/captures/B07_employee_incident_autre_403.png" alt="Accès par URL à l'incident d'un autre : refus du backend (403)"><br><i>Figure 62 : Accès par URL à l'incident d'un autre : refus du backend (403)</i></p>
<p align="center"><img src="rapport/captures/B08_employee_route_admin_forbidden.png" alt="Accès à /admin/users avec le rôle EMPLOYEE : page 403"><br><i>Figure 63 : Accès à /admin/users avec le rôle EMPLOYEE : page 403</i></p>
<p align="center"><img src="rapport/captures/B09_profil.png" alt="Profil et changement de mot de passe"><br><i>Figure 64 : Profil et changement de mot de passe</i></p>

### 9.3 Administrateur (ADMIN)
<p align="center"><img src="rapport/captures/C01_admin_dashboard.png" alt="Dashboard ADMIN : utilisateurs et incidents par statut"><br><i>Figure 65 : Dashboard ADMIN : utilisateurs et incidents par statut</i></p>
<p align="center"><img src="rapport/captures/C02_admin_tous_incidents.png" alt="Tous les incidents, filtre et affectation"><br><i>Figure 66 : Tous les incidents, filtre et affectation</i></p>
<p align="center"><img src="rapport/captures/C03_admin_utilisateurs.png" alt="Gestion des utilisateurs : activation et désactivation"><br><i>Figure 67 : Gestion des utilisateurs : activation et désactivation</i></p>
<p align="center"><img src="rapport/captures/C04_admin_affectation.png" alt="Affectation d'un incident à un technicien"><br><i>Figure 68 : Affectation d'un incident à un technicien</i></p>

### 9.4 Technicien (TECHNICIAN)
<p align="center"><img src="rapport/captures/D01_technicien_dashboard.png" alt="Dashboard TECHNICIAN : affectés, critiques, en cours, résolus"><br><i>Figure 69 : Dashboard TECHNICIAN : affectés, critiques, en cours, résolus</i></p>
<p align="center"><img src="rapport/captures/D02_technicien_incidents_affectes.png" alt="Incidents affectés au technicien"><br><i>Figure 70 : Incidents affectés au technicien</i></p>
<p align="center"><img src="rapport/captures/D03_technicien_traitement.png" alt="Traitement : passage à RESOLVED et notes"><br><i>Figure 71 : Traitement : passage à RESOLVED et notes</i></p>
<p align="center"><img src="rapport/captures/D04_technicien_incident_non_affecte.png" alt="Incident affecté à un autre technicien : accès refusé"><br><i>Figure 72 : Incident affecté à un autre technicien : accès refusé</i></p>

### 9.5 Vérification finale et expiration de session
<p align="center"><img src="rapport/captures/B10_employee_etat_final_historique.png" alt="L'employé voit l'état RESOLVED, l'historique complet et les notes"><br><i>Figure 73 : L'employé voit l'état RESOLVED, l'historique complet et les notes</i></p>
<p align="center"><img src="rapport/captures/E01_session_expiree.png" alt="JWT expiré : suppression du token, redirection vers /login et message « session expirée »"><br><i>Figure 74 : JWT expiré : suppression du token, redirection vers /login et message « session expirée »</i></p>

---

## 10. Endpoints de l'API

| Méthode | URL | Accès | Description |
|---------|-----|-------|-------------|
| POST | `/auth/register` | Public | Inscription (rôle EMPLOYEE) |
| POST | `/auth/login` | Public | Connexion → token, refreshToken, role, fullName |
| POST | `/auth/refresh` | Refresh token | Nouvel access token (rotation) |
| POST | `/auth/logout` | Refresh token | Révocation du refresh token |
| GET / PUT | `/api/profile` | Authentifié | Consulter / modifier son profil |
| PUT | `/api/profile/password` | Authentifié | Changer son mot de passe |
| GET | `/api/dashboard` | Authentifié | Statistiques selon le rôle |
| POST | `/api/incidents` | EMPLOYEE | Déclarer un incident |
| GET | `/api/incidents/my` | EMPLOYEE | Mes incidents |
| GET | `/api/incidents/{id}` | Propriétaire / affecté / ADMIN | Détail + historique + notes |
| GET | `/api/incidents/assigned` | TECHNICIAN | Incidents affectés |
| PUT | `/api/incidents/{id}/status` | TECHNICIAN (affecté) ou ADMIN | Changer le statut |
| PUT | `/api/incidents/{id}/assign/{technicianId}` | ADMIN | Affecter à un technicien |
| POST | `/api/incidents/{id}/comments` | TECHNICIAN (affecté) ou ADMIN | Ajouter une note |
| GET | `/api/incidents?status=` | ADMIN | Tous les incidents |
| GET | `/api/admin/users` | ADMIN | Liste des utilisateurs |
| GET | `/api/admin/technicians` | ADMIN | Techniciens actifs |
| PUT | `/api/admin/users/{id}/enable` · `/disable` | ADMIN | Activer / désactiver un compte |
| GET | `/api/admin/statistics` | ADMIN | Statistiques globales |

---

## 11. Bilan de sécurité

| Menace | Contre-mesure |
|--------|---------------|
| Vol de mots de passe en base | Hachage BCrypt salé ; le mot de passe n'est jamais renvoyé |
| Falsification du token | Signature HMAC-SHA256 ; tout payload modifié est rejeté (testé) |
| Rejeu d'un token volé | Durée de vie de 5 minutes ; rotation et révocation du refresh token |
| Compte compromis | Désactivation par l'admin, effet immédiat |
| Accès direct par identifiant (IDOR) | Contrôle du propriétaire / de l'affectation dans le service |
| Escalade de rôle à l'inscription | Aucun champ `role` dans le DTO |
| Contournement du frontend | Toutes les règles sont dans le backend |
| Données malformées | Bean Validation, enums typés, requêtes JPA paramétrées |
| Fuite d'informations techniques | Messages d'erreur maîtrisés, pas de stack trace |
| Requêtes cross-origin | CORS limité à `http://localhost:5173` |

**Améliorations possibles** : refresh token dans un cookie `HttpOnly` (le `localStorage` est exposé aux failles XSS), HTTPS obligatoire, secret JWT en variable d'environnement, limitation des tentatives de connexion, liste noire des access tokens à la déconnexion.

---

## 12. Conclusion

Ce projet met en œuvre une application complète et sécurisée : authentification **stateless** par JWT signé, autorisation par **rôles** sur deux niveaux, **contrôle du propriétaire** des données côté serveur, validation des entrées, gestion centralisée des erreurs, **refresh token** avec rotation et historique des incidents. Côté React, les intercepteurs Axios automatisent l'envoi du JWT et la gestion de l'expiration.

Les tests (9 tests d'intégration, 94 assertions Postman, tests curl et scénario complet dans l'interface) confirment que toutes les exigences du sujet sont respectées, en particulier les **9 tests de sécurité obligatoires**.

> **La sécurité se décide toujours côté serveur** : un JWT valide prouve une identité, mais chaque accès aux données doit encore être autorisé selon le rôle, la propriété et l'état de la ressource.

---

<p align="center">
  📄 Rapport complet : <a href="rapport/Hicham_OUAOUCHE_JWT_Spring_Security_React.pdf"><b>Hicham_OUAOUCHE_JWT_Spring_Security_React.pdf</b></a><br>
  <sub>Hicham OUAOUCHE · ENSET Mohammedia · SDIA · 2026-2027</sub>
</p>
