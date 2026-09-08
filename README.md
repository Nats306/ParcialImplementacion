# 🐪 The Great EIA Camel vs. Dwarf Racing System

REST API developed in Java with Spring Boot for managing a racing competition involving camels and dwarfs.

The system manages competitors, teams, races, race registrations, official results, statistics, and standings while enforcing the business rules defined for the competition.

The project uses a layered architecture and PostgreSQL persistence through Docker.

---

## 📌 Project Status

### Implemented and manually tested

| Module / Feature | Status |
|---|---|
| Competitor management | ✅ Implemented and tested |
| Team management | ✅ Implemented and tested |
| Team ↔ Competitor relationship | ✅ Implemented and tested |
| Race management | ✅ Implemented and tested |
| Race lifecycle | ✅ Implemented and tested |
| Race Registration | ✅ Implemented and tested |
| Registration approval | ✅ Implemented and tested |
| Registration business rules | ✅ Implemented and tested |
| Race Results | ✅ Implemented and tested |
| Team results | ✅ Implemented and tested |
| Individual competitor results | ✅ Implemented and tested |
| Penalty and total time calculation | ✅ Implemented and tested |
| Points calculation | ✅ Implemented and tested |
| Competitor statistics | ✅ Implemented and tested |
| Team statistics | ✅ Implemented and tested |
| Standings | ✅ Implemented and tested |
| PostgreSQL persistence | ✅ Implemented and tested |
| PostgreSQL with Docker | ✅ Implemented and tested |
| HTTP error handling | ✅ Implemented |
| Validation | ✅ Implemented |
| Security / User / Role | ❌ Pending |
| Audit Log | ❌ Pending |
| Automated tests | ❌ Pending |
| Full backend Docker image | ⏳ Pending |
| GUI | ❌ Pending |
| Final authentication/authorization integration | ❌ Pending |

---

# 🛠️ Technologies

- Java 21+
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- PostgreSQL
- Docker
- Docker Compose
- Gradle
- Lombok
- PowerShell for manual API testing
- IntelliJ IDEA

---

# 🏗️ Architecture

The backend follows a layered architecture.

```text
Client / GUI
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
JPA / Hibernate
     │
     ▼
PostgreSQL
```

Controllers are responsible for receiving HTTP requests and returning responses.

Business rules are implemented in the service layer.

Repositories provide persistence through Spring Data JPA.

Entities are not directly exposed through the REST API. DTOs and mappers are used between the API and persistence layers.

---

# 📂 Project Structure

The main backend structure is organized approximately as follows:

```text
src/main/java/com/parcialimplementacion/parcialenanosvscamellos/

├── common/
│   └── exceptions/
│
├── competitor/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   ├── service/
│   └── specification/
│
├── team/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   ├── service/
│   └── specification/
│
├── race/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   ├── service/
│   └── specification/
│
├── registration/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   └── service/
│
├── result/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── mapper/
│   ├── repository/
│   ├── service/
│   └── util/
│
└── standings/
    ├── controller/
    ├── dto/
    └── service/
```

---

# 🗄️ Database

The application currently uses PostgreSQL running inside Docker.

The database container is based on:

```text
postgres:16-alpine
```

Hibernate is currently configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This means that Hibernate creates or updates the required database tables automatically during development.

Some of the main tables created by the system are:

```text
competitors
teams
races
race_registrations
race_results
```

---

# 🐳 PostgreSQL with Docker

The project contains a `compose.yml` file used to start PostgreSQL.

Start the database with:

```powershell
docker compose up -d
```

Check container status:

```powershell
docker compose ps
```

The database container should appear as healthy.

Example:

```text
db    postgres:16-alpine    Up (healthy)
```

---

# ⚙️ Environment Variables

Sensitive database credentials must not be committed to the repository.

The real `.env` file must remain ignored by Git.

Example `.env.example`:

```env
DB_HOST=127.0.0.1
DB_NAME=camel_racing
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
DB_PORT=5433
```

Create a local `.env` based on `.env.example`.

Example:

```text
.env.example  → committed
.env          → NOT committed
```

The `.gitignore` should contain:

```gitignore
.env
```

---

# 🔌 Database Configuration

The application uses environment variables in `application.properties`.

Example:

```properties
spring.application.name=ParcialEnanosvsCamellos

spring.config.import=optional:file:./.env[.properties]

spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

spring.web.error.include-stacktrace=never
spring.web.error.include-exception=false
```

---

# ▶️ Running the Application

## 1. Start PostgreSQL

From the project root:

```powershell
docker compose up -d
```

Verify:

```powershell
docker compose ps
```

---

## 2. Configure database variables

The project can use the values contained in `.env`.

If necessary, the variables can also be configured directly in PowerShell before starting Spring Boot.

Example:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5433/camel_racing"
$env:SPRING_DATASOURCE_USERNAME="your_database_user"
$env:SPRING_DATASOURCE_PASSWORD="your_database_password"
```

Do not commit real passwords to GitHub.

---

## 3. Start Spring Boot

From the project root:

```powershell
.\gradlew bootRun
```

The application will be available at:

```text
http://localhost:8080
```

The terminal running `bootRun` must remain open.

Gradle may display something similar to:

```text
80% EXECUTING
```

while the server is running.

This is normal. It means Spring Boot is active and waiting for HTTP requests.

---

# ⚠️ IntelliJ Run Configuration

At the current development stage, the application has been tested successfully using:

```powershell
.\gradlew bootRun
```

The standard green Run button in IntelliJ may still require additional datasource/environment-variable configuration.

This does **not** prevent the backend from running.

The tested execution method is:

```text
Docker PostgreSQL
       +
Gradle bootRun
       +
PowerShell HTTP requests
```

---

# 🧪 Manual Testing

Manual integration tests were executed using PowerShell `Invoke-RestMethod`.

The process used was:

```text
PowerShell HTTP Request
        ↓
REST Controller
        ↓
Service
        ↓
Business Rules
        ↓
Repository
        ↓
Hibernate / JPA
        ↓
PostgreSQL
```

Database persistence was additionally verified directly using PostgreSQL commands inside the Docker container.

---

# 🧍 Competitors

Competitors can be created, queried, updated and assigned to teams.

Supported competitor types include:

```text
CAMEL
DWARF
```

Competitor status includes values such as:

```text
ACTIVE
RETIRED
```

The system also maintains statistics including:

```text
victories
defeats
completedRaces
```

---

## Example: Create Competitor

```powershell
$body = @{
    name = "Byte"
    nickname = "byte-camel"
    competitorType = "CAMEL"
    age = 10
    weight = 450.0
    height = 2.1
    country = "Colombia"
} | ConvertTo-Json
```

```powershell
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/competitors" `
    -ContentType "application/json" `
    -Body $body
```

---

## Example: Get Competitors

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/competitors"
```

---

# 👥 Teams

Teams can be created and can contain competitors.

A competitor cannot simultaneously belong to multiple active teams.

Teams also maintain race statistics:

```text
victories
defeats
```

---

## Example: Create Team

```powershell
$teamBody = @{
    name = "The Five Exceptions"
    description = "Test team"
    coach = "Ada Coach"
} | ConvertTo-Json
```

```powershell
$team = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/teams" `
    -ContentType "application/json" `
    -Body $teamBody
```

---

## Add Competitor to Team

```powershell
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/teams/$teamId/members/$competitorId"
```

The relationship was verified directly in PostgreSQL.

---

# 🏁 Races

Races support a lifecycle controlled through race status.

Possible statuses include:

```text
DRAFT
OPEN_FOR_REGISTRATION
CLOSED_FOR_REGISTRATION
IN_PROGRESS
COMPLETED
CANCELLED
```

Valid lifecycle example:

```text
DRAFT
  ↓
OPEN_FOR_REGISTRATION
  ↓
CLOSED_FOR_REGISTRATION
  ↓
IN_PROGRESS
  ↓
COMPLETED
```

Cancellation is also supported from valid intermediate states.

---

## Important Race Rules

Some implemented rules include:

- A race cannot start without at least two approved participants.
- Registration must occur before the registration deadline.
- Registrations can only occur while the race is open.
- A completed race cannot continue changing state.
- A race cannot be completed without official results.
- A race in progress or completed cannot be deleted.
- Invalid state transitions are rejected.

---

## Example: Create Race

```powershell
$raceDate = (Get-Date).AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss")
$deadline = (Get-Date).AddDays(6).ToString("yyyy-MM-ddTHH:mm:ss")

$raceBody = @{
    name = "First Mixed Race"
    description = "Integration test race"
    scheduledDateTime = $raceDate
    startLocation = "Universidad EIA"
    finishLocation = "Alto de Las Palmas"
    distanceMeters = 1000
    maxParticipants = 10
    raceType = "MIXED"
    organizer = "Test Organizer"
    registrationDeadline = $deadline
} | ConvertTo-Json
```

```powershell
$race = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/races" `
    -ContentType "application/json" `
    -Body $raceBody
```

---

# 📝 Race Registration

Race Registration supports both:

```text
Individual Competitor
Team
```

Registration statuses:

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

---

## Implemented Registration Rules

The system validates:

- Registration is only allowed for an open race.
- Registration must occur before the deadline.
- A competitor or team cannot be registered twice in the same race.
- Starting positions cannot be duplicated.
- Only eligible competitors and teams can register.
- Race participant type must be compatible with race type.
- A participant cannot participate individually and as part of a team in the same race.
- Rejected registrations require validation information.
- Race capacity must not be exceeded.

---

## Registration Endpoints

```text
POST   /api/races/{raceId}/registrations
GET    /api/races/{raceId}/registrations
GET    /api/registrations/{id}
PATCH  /api/registrations/{id}/approve
PATCH  /api/registrations/{id}/reject
DELETE /api/registrations/{id}
```

---

## Example: Register Team

```powershell
$teamRegistrationBody = @{
    teamId = $teamId
    startingPosition = 1
    registeredBy = "test-organizer"
} | ConvertTo-Json
```

```powershell
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/races/$raceId/registrations" `
    -ContentType "application/json" `
    -Body $teamRegistrationBody
```

The initial registration status is:

```text
PENDING
```

---

## Approve Registration

```powershell
Invoke-RestMethod `
    -Method Patch `
    -Uri "http://localhost:8080/api/registrations/$registrationId/approve"
```

The new status becomes:

```text
APPROVED
```

---

# 🏆 Race Results

Official race results can only be created for approved participants while the race is:

```text
IN_PROGRESS
```

Supported result statuses:

```text
FINISHED
DISQUALIFIED
DID_NOT_FINISH
DID_NOT_START
```

Each result contains information such as:

```text
race
registration
starting position
final position
completion time
penalty time
result status
notes
recorded by
recorded timestamp
```

---

# ⏱️ Time Calculation

For finished participants:

```text
totalTimeMillis =
completionTimeMillis + penaltyTimeMillis
```

Example:

```text
completionTimeMillis = 65000
penaltyTimeMillis    = 1000

totalTimeMillis      = 66000
```

---

# 🥇 Result Rules

Implemented validations include:

- Results can only be entered while the race is `IN_PROGRESS`.
- Only approved registrations may receive official results.
- A registration can only have one official result.
- Normal final positions cannot be duplicated.
- Completion time must be positive.
- Only one participant may finish in position `1`.
- `FINISHED` requires a final position.
- `FINISHED` requires a completion time.
- A participant marked `DID_NOT_START` cannot have a completion time.
- Results cannot be modified once the race is completed.

---

# 🎯 Points System

The scoring table is:

| Position | Points |
|---:|---:|
| 1st | 10 |
| 2nd | 7 |
| 3rd | 5 |
| 4th | 3 |
| 5th | 1 |
| Other | 0 |

Only participants with:

```text
resultStatus = FINISHED
```

receive position points.

---

# 🏆 Result Endpoints

```text
POST /api/races/{raceId}/results
GET  /api/races/{raceId}/results
GET  /api/results/{id}
PUT  /api/results/{id}
```

---

## Example: Team Result

```powershell
$teamResultBody = @{
    registrationId = 1
    resultStatus = "FINISHED"
    finalPosition = 1
    completionTimeMillis = 60000
    penaltyTimeMillis = 0
    notes = "Team finished first"
    recordedBy = "test-organizer"
} | ConvertTo-Json
```

```powershell
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/races/$raceId/results" `
    -ContentType "application/json" `
    -Body $teamResultBody
```

Expected result:

```text
finalPosition        : 1
completionTimeMillis : 60000
penaltyTimeMillis    : 0
totalTimeMillis      : 60000
resultStatus         : FINISHED
points               : 10
```

---

## Example: Individual Result

```powershell
$competitorResultBody = @{
    registrationId = 2
    resultStatus = "FINISHED"
    finalPosition = 2
    completionTimeMillis = 65000
    penaltyTimeMillis = 1000
    notes = "Competitor finished second"
    recordedBy = "test-organizer"
} | ConvertTo-Json
```

Expected result:

```text
finalPosition        : 2
completionTimeMillis : 65000
penaltyTimeMillis    : 1000
totalTimeMillis      : 66000
points               : 7
```

---

# 📊 Standings

The application automatically calculates standings from official race results.

Competitors and teams have independent rankings.

Available endpoints:

```text
GET /api/standings
GET /api/standings/competitors
GET /api/standings/teams
```

---

## Example

```powershell
$standings = Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/api/standings"

$standings | ConvertTo-Json -Depth 6
```

Example output:

```json
{
  "competitors": [
    {
      "rank": 1,
      "name": "Null Pointer",
      "nickname": "null-pointer",
      "points": 7,
      "victories": 0,
      "secondPlaces": 1,
      "thirdPlaces": 0,
      "racesWithResults": 1
    }
  ],
  "teams": [
    {
      "rank": 1,
      "name": "The Five Exceptions",
      "points": 10,
      "victories": 1,
      "secondPlaces": 0,
      "thirdPlaces": 0,
      "racesWithResults": 1
    }
  ]
}
```

---

# 📈 Statistics

Official results automatically update participant statistics.

For competitors:

```text
victories
defeats
completedRaces
```

Example tested result:

```text
Null Pointer

victories      : 0
defeats        : 1
completedRaces : 1
```

For teams:

```text
victories
defeats
```

Example tested result:

```text
The Five Exceptions

victories : 1
defeats   : 0
```

---

# 🚫 Business Rule Error Handling

The application uses centralized exception handling.

Expected HTTP statuses include:

```text
400 Bad Request
404 Not Found
409 Conflict
```

Examples of operations that return `409 Conflict` include:

- Duplicate registration.
- Duplicate starting position.
- Duplicate final position.
- More than one race winner.
- Invalid race status transition.
- Entering results for a race that is not in progress.
- Editing official results after the race has been completed.

Spring stack traces are disabled from normal API responses.

---

# ✅ Manual Tests Performed

The following flows were manually tested successfully:

```text
Competitor                         ✅ tested
Team                               ✅ tested
Team ↔ Competitor                  ✅ tested
Race                               ✅ tested
RaceRegistration                   ✅ tested
PENDING → APPROVED                 ✅ tested
minimum 2 approved to start        ✅ tested
duplicate starting position → 409  ✅ tested
RaceResult TEAM                    ✅ tested
RaceResult COMPETITOR              ✅ tested
penalty and total time             ✅ tested
points 10 / 7                      ✅ tested
PostgreSQL persistence             ✅ tested
Standings                          ✅ tested
statistics update                  ✅ tested
Race → COMPLETED with results      ✅ tested
duplicate winner → 409             ✅ tested
edit result after COMPLETED → 409  ✅ tested
```

---

# 🔎 PostgreSQL Verification

Persistence was not only validated through API responses.

The database was queried directly inside the Docker container.

## Competitors

```powershell
docker compose exec db psql `
    -U your_database_user `
    -d camel_racing `
    -c "select * from competitors;"
```

---

## Registrations

```powershell
docker compose exec db psql `
    -U your_database_user `
    -d camel_racing `
    -c "select * from race_registrations;"
```

---

## Results

```powershell
docker compose exec db psql `
    -U your_database_user `
    -d camel_racing `
    -c "select * from race_results;"
```

This allowed verification that API operations were actually persisted in PostgreSQL.

---

# 🔐 Security — Pending

Authentication and authorization are still pending.

The final implementation must include concepts such as:

```text
User
Role
authentication
authorization
protected endpoints
```

Once implemented, fields currently sent manually such as:

```text
registeredBy
recordedBy
```

should ideally be obtained from the authenticated user instead of being provided directly by the client.

---

# 🧾 Audit Log — Pending

Audit logging is also pending.

The final system should record important actions such as:

- Authentication events.
- Creation and modification of competitors.
- Team changes.
- Race status changes.
- Registration approvals/rejections.
- Result creation/modification.
- Important administrative operations.

---

# 🧪 Automated Tests — Pending

Manual integration tests have already been performed.

Automated tests are still pending.

The final project should include meaningful tests for service logic and important business rules.

Examples:

```text
create competitor
duplicate nickname
create team
add competitor to team
duplicate team membership
create race
invalid race transition
registration before deadline
duplicate starting position
race start with fewer than two participants
race start with valid participants
result for unapproved registration
duplicate final position
duplicate winner
points calculation
race completion without results
race completion with results
```

---

# 🖥️ GUI — Pending

The graphical interface has not yet been implemented.

The final GUI should communicate exclusively with the REST API.

The GUI must **not access PostgreSQL directly**.

Expected main flows include:

```text
Login
Competitors
Teams
Races
Registrations
Results
Standings
```

---

# 🐳 Final Dockerization — Pending

Currently PostgreSQL runs through Docker.

The Spring Boot backend is currently started locally with:

```powershell
.\gradlew bootRun
```

For the final delivery, the backend should also be dockerized.

The final goal is:

```powershell
docker compose up -d
```

and have that single command start at least:

```text
Backend
PostgreSQL
```

without requiring a separate manual `bootRun`.

---

# 🔄 Current Development Workflow

Current tested workflow:

```text
1. docker compose up -d
          ↓
2. PostgreSQL starts
          ↓
3. configure environment variables
          ↓
4. .\gradlew bootRun
          ↓
5. Spring Boot starts at localhost:8080
          ↓
6. Use another PowerShell terminal
          ↓
7. Send REST requests with Invoke-RestMethod
          ↓
8. Verify API responses
          ↓
9. Verify persisted records directly in PostgreSQL
```

---

# 🧹 Stop the Application

Stop Spring Boot in the terminal running `bootRun` with:

```text
Ctrl + C
```

Stop Docker containers with:

```powershell
docker compose down
```

If the database volume should remain preserved, `docker compose down` is enough.

Do not remove the PostgreSQL volume unless intentionally resetting the database.

---

# 🌐 Main API Routes

Current main API areas:

```text
/api/competitors
/api/teams
/api/races
/api/races/{raceId}/registrations
/api/registrations
/api/races/{raceId}/results
/api/results
/api/standings
```

---

# 📌 Important Notes

- PostgreSQL currently uses host port `5433` in the tested local environment.
- PostgreSQL internally still listens on port `5432`.
- Real database credentials must never be committed.
- `.env` must remain ignored.
- `.env.example` may be committed with placeholder values.
- The application has been successfully executed using Gradle `bootRun`.
- The IntelliJ green Run configuration may require additional local datasource configuration.
- Security and authorization are still pending.
- Automated tests are still pending.
- GUI development is still pending.
- Final full-stack Docker Compose configuration is still pending.

---

# 👩‍💻 Development Progress

Current backend domain flow:

```text
Competitor ✅
     │
     ▼
Team ✅
     │
     ▼
Race ✅
     │
     ▼
RaceRegistration ✅
     │
     ▼
RaceResult ✅
     │
     ▼
Statistics ✅
     │
     ▼
Standings ✅
```

Remaining major components:

```text
Security / User / Role ❌
AuditLog ❌
Automated Tests ❌
GUI ❌
Final Dockerization ⏳
```

---

# 📦 Repository

Clone the repository:

```bash
git clone https://github.com/Nats306/ParcialImplementacion.git
```

Enter the project directory:

```bash
cd ParcialImplementacion
```

Then follow the Docker and application startup instructions described above.

---

# 👥 Authors

Developed as part of the implementation project for the EIA racing system assignment.

Team members should add their names here:

```text
- [Student Name]
- [Student Name]
```

---

# 📄 Academic Project

This repository was developed for academic purposes as part of a software implementation assignment.

The objective is to demonstrate:

- REST API design
- Layered backend architecture
- Business rule implementation
- Relational persistence
- JPA/Hibernate usage
- Docker integration
- Validation
- Exception handling
- Race lifecycle management
- Results and standings calculation
- Authentication and authorization
- Automated testing
- GUI/API integration
