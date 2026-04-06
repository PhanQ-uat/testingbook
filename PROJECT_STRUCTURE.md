# testerBook - Source Code Structure

> **Learning Management System for Software Testing Students**
> 
> **Tech Stack:** Spring Boot 3.2.5, Java 17, Spring Security (JWT), Spring Data JPA, Thymeleaf, H2/PostgreSQL

---

## 📁 Project Structure

```
testerBook/
├── pom.xml                              # Maven configuration
├── docker-compose.yml                   # Docker setup for PostgreSQL
├── database-setup.md                    # Database initialization guide
├── src/
│   ├── main/
│   │   ├── java/com/testerbook/
│   │   │   ├── TesterBookApplication.java          # Application entry point
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java            # Default data setup
│   │   │   │   └── SecurityConfig.java             # Security & JWT config
│   │   │   ├── controller/                         # REST & Web Controllers
│   │   │   │   ├── AuthController.java             # Login/Register APIs
│   │   │   │   ├── DatabaseController.java         # Admin CRUD APIs
│   │   │   │   ├── PublicPostController.java       # Public training posts API
│   │   │   │   ├── SettingsController.java         # Site settings API
│   │   │   │   ├── UserController.java             # User profile APIs
│   │   │   │   └── WebController.java              # Page routing
│   │   │   ├── dto/                                # Data Transfer Objects
│   │   │   │   ├── AuthRequest.java                # Login request
│   │   │   │   ├── AuthResponse.java               # Login response (JWT)
│   │   │   │   └── RegisterRequest.java            # Registration request
│   │   │   ├── model/                              # JPA Entities
│   │   │   │   ├── Activity.java                   # User activities log
│   │   │   │   ├── Feedback.java                   # Post feedback/comments
│   │   │   │   ├── LearningPhase.java              # Enum: PHASE_1, PHASE_2, PHASE_3, ADVANCED
│   │   │   │   ├── PostCategory.java               # Enum: DOCUMENTATION, BUG_TRACKING, STLC, TEST_CASES, AUTOMATION
│   │   │   │   ├── PostStatus.java                 # Enum: DRAFT, PUBLISHED, ARCHIVED
│   │   │   │   ├── SiteSettings.java               # Dynamic site configuration
│   │   │   │   ├── Tag.java                        # Post tags
│   │   │   │   ├── TrainingPost.java               # Training content posts
│   │   │   │   ├── User.java                       # User entity
│   │   │   │   └── UserRole.java                   # Enum: STUDENT, ADMIN
│   │   │   ├── repository/                         # JPA Repositories
│   │   │   │   ├── ActivityRepository.java
│   │   │   │   ├── FeedbackRepository.java
│   │   │   │   ├── SiteSettingsRepository.java
│   │   │   │   ├── TagRepository.java
│   │   │   │   ├── TrainingPostRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/                           # Security Layer
│   │   │   │   ├── CustomUserDetailsService.java   # UserDetailsService impl
│   │   │   │   ├── JwtAuthenticationFilter.java    # JWT filter
│   │   │   │   └── JwtUtil.java                     # JWT utilities
│   │   │   └── service/                            # Business Logic
│   │   │       └── AuthService.java
│   │   └── resources/
│   │       ├── application.properties             # App configuration
│   │       ├── static/                            # Static assets (CSS, JS, images)
│   │       └── templates/                         # Thymeleaf Templates
│   │           ├── about.html                     # About page
│   │           ├── admin.html                     # Admin dashboard
│   │           ├── dashboard.html                 # User dashboard
│   │           ├── index.html                     # Home page
│   │           ├── login.html                     # Login page
│   │           ├── plans.html                     # Learning plans page
│   │           ├── profile.html                   # User profile
│   │           ├── projects.html                  # Projects page
│   │           ├── register.html                  # Registration page
│   │           └── training.html                  # Self-learning page
│   └── test/                                      # Unit tests
└── target/                                        # Build output
```

---

## 🔐 Authentication Flow

```
┌─────────────┐      POST /api/auth/login       ┌─────────────┐
│   Login Page  │ ───────────────────────────────→ │ AuthController│
│  (login.html) │                                  │  (JWT Token)  │
└─────────────┘                                  └─────────────┘
       │                                               │
       │         Store token in localStorage           │
       │←──────────────────────────────────────────────┘
       │
       ↓
┌─────────────┐      Bearer Token Header        ┌─────────────┐
│  Dashboard    │ ───────────────────────────────→ │ API Endpoints │
│(dashboard.html)│                                │  (Secured)    │
└─────────────┘                                  └─────────────┘
```

---

## 📊 Entity Relationships (ER Diagram)

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│      User       │       │   TrainingPost   │       │    Feedback     │
├─────────────────┤       ├──────────────────┤       ├─────────────────┤
│ id (PK)         │──────<│ id (PK)          │>──────│ id (PK)         │
│ username        │   1:M  │ title            │   1:M │ content         │
│ email           │       │ content          │       │ rating          │
│ password        │       │ phase            │       │ created_at      │
│ role            │       │ category         │       │ user_id (FK)    │
│ firstName       │       │ status           │       │ post_id (FK)    │
│ lastName        │       │ author_id (FK)   │       └─────────────────┘
│ bio             │       │ postDate         │
│ created_at      │       └──────────────────┘
└─────────────────┘                ↑
       │                           │
       │ 1:M                       │ M:1
       ↓                           │
┌─────────────────┐                │
│    Activity     │                │
├─────────────────┤                │
│ id (PK)         │                │
│ activityType    │                │
│ description     │                │
│ activityDate    │                │
│ user_id (FK)    │                │
└─────────────────┘                │
                                   │
┌─────────────────┐                │
│      Tag        │>───────────────┘
├─────────────────┤      M:M (via join table)
│ id (PK)         │
│ name            │
└─────────────────┘
```

---

## 🌐 API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login, returns JWT |
| POST | `/api/auth/register` | User registration |

### User (`/api/users`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Get current user profile |
| GET | `/api/users/check-username/{username}` | Check username availability |
| GET | `/api/users/check-email/{email}` | Check email availability |

### Admin (`/api/admin`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/training-posts` | List all posts |
| POST | `/api/admin/training-posts` | Create new post |
| PUT | `/api/admin/training-posts/{id}` | Update post |
| DELETE | `/api/admin/training-posts/{id}` | Delete post |

### Public (`/api/posts/public`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/posts/public/all` | Get all published posts |
| GET | `/api/posts/public/{id}` | Get post by ID |

### Settings (`/api/settings`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/settings/all` | Get all settings |
| POST | `/api/settings/save` | Save settings |

---

## 🎨 Frontend Pages

| Page | Template | Description |
|------|----------|-------------|
| Home | `index.html` | Landing page with hero section |
| About | `about.html` | About the platform |
| Login | `login.html` | User login form |
| Register | `register.html` | User registration form |
| Dashboard | `dashboard.html` | User dashboard with overview widgets |
| Training | `training.html` | Self-learning content browser |
| Plans | `plans.html` | Learning roadmap & milestones |
| Projects | `projects.html` | Project showcase |
| Profile | `profile.html` | User profile management |
| Admin | `admin.html` | Admin panel for content management |

---

## 🔧 Key Features Implemented

### ✅ Core Features
- [x] User Registration & Login (JWT-based)
- [x] Role-based Access (STUDENT, ADMIN)
- [x] Training Posts CRUD
- [x] Activity Logging
- [x] Dynamic Site Settings (Site Name, Description)

### ✅ Dashboard Overview Widgets
- [x] Phase Progress Tracker (P1→P2→P3→Advanced)
- [x] Activity Heatmap (GitHub-style)
- [x] 30-Day Progress Analytics Chart
- [x] Gamification (Streak counter, Achievement badges)

### ✅ Security
- [x] JWT Authentication
- [x] Password Encryption (BCrypt)
- [x] CORS Configuration
- [x] Protected API Endpoints

---

## 🚀 Running the Application

```bash
# Build & Run
mvn spring-boot:run

# Or build JAR
mvn clean package
java -jar target/testerBook-1.0-SNAPSHOT.jar
```

**Access URLs:**
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

---

## 📝 Notes

- **Database:** H2 (dev) / PostgreSQL (prod)
- **JWT Secret:** Configured in `application.properties`
- **Default Admin:** Created via `DataInitializer.java`
- **Frontend:** Uses Bootstrap 5, Font Awesome, Vanilla JavaScript
- **Dynamic Site Name:** All pages load site name from `/api/settings/all`

