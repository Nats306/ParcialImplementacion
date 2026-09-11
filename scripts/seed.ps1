# ===================================================================
# Script de datos semilla — The Great EIA Camel vs. Dwarf Racing System
# ===================================================================
# Requiere que la app esté corriendo en http://localhost:8080
# (docker compose up -d, o desde IntelliJ) y que el usuario "admin"
# exista en Keycloak con la contraseña de abajo.
#
# Uso: .\scripts\seed.ps1

$baseUrl = "http://localhost:8080"

function Invoke-Api {
    param($Method, $Path, $Body = $null, $Token)
    $headers = @{ Authorization = "Bearer $Token" }
    try {
        if ($Body) {
            $json = $Body | ConvertTo-Json -Depth 5
            $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
            return Invoke-RestMethod -Uri "$baseUrl$Path" -Method $Method -Headers $headers -ContentType "application/json; charset=utf-8" -Body $bytes
        } else {
            return Invoke-RestMethod -Uri "$baseUrl$Path" -Method $Method -Headers $headers
        }
    } catch {
        Write-Host "ERROR en $Method $Path" -ForegroundColor Red
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
            Write-Host $reader.ReadToEnd() -ForegroundColor Red
        } else {
            Write-Host $_.Exception.Message -ForegroundColor Red
        }
        throw
    }
}

# --- Login ---
Write-Host "== Login ==" -ForegroundColor Cyan
$loginBody = @{ username = "admin"; password = "Admin123!" } | ConvertTo-Json
$token = (Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody).access_token

# --- Competidores ---
Write-Host "== Creando competidores ==" -ForegroundColor Cyan

$dwarfsData = @(
    @{ name="Thorin Barbahierro"; nickname="Trueno"; competitorType="DWARF"; age=34; weight=78; height=1.35; country="Colombia" },
    @{ name="Grendal Forjaoro"; nickname="Relámpago"; competitorType="DWARF"; age=29; weight=82; height=1.40; country="Colombia" },
    @{ name="Bruna Yunquefuerte"; nickname="Ventisca"; competitorType="DWARF"; age=27; weight=70; height=1.30; country="España" },
    @{ name="Fendrel Pico de Piedra"; nickname="Avalancha"; competitorType="DWARF"; age=41; weight=85; height=1.45; country="Noruega" },
    @{ name="Ithil Manoshabiles"; nickname="Centella"; competitorType="DWARF"; age=25; weight=68; height=1.28; country="Colombia" }
)

$camelsData = @(
    @{ name="Sahara Reina del Desierto"; nickname="Sahara"; competitorType="CAMEL"; age=6; weight=520; height=2.10; country="Marruecos" },
    @{ name="Simoun Tormenta de Arena"; nickname="Simún"; competitorType="CAMEL"; age=8; weight=580; height=2.20; country="Egipto" }
)

$mediumData = @(
    @{ name="Kael Vientoligero"; nickname="Kael"; competitorType="MEDIUM"; age=22; weight=68; height=1.75; country="Colombia" },
    @{ name="Dana Piesveloces"; nickname="Dana"; competitorType="MEDIUM"; age=24; weight=62; height=1.68; country="Colombia" }
)

$competitors = @{}
foreach ($c in $dwarfsData + $camelsData + $mediumData) {
    $resp = Invoke-Api -Method Post -Path "/api/competitors" -Body $c -Token $token
    $competitors[$c.nickname] = $resp.id
    Write-Host "  Creado: $($c.name) [$($c.competitorType)] -> $($resp.id)"
}

# --- Equipos ---
Write-Host "== Creando equipos ==" -ForegroundColor Cyan

$team1 = Invoke-Api -Method Post -Path "/api/teams" -Body @{
    name = "Los Forjadores del Trueno"
    description = "Equipo especializado en velocistas enanos."
    coach = "Balin Escudoférreo"
} -Token $token
Write-Host "  Equipo creado: $($team1.name) -> $($team1.id)"

$team2 = Invoke-Api -Method Post -Path "/api/teams" -Body @{
    name = "Caravana del Viento"
    description = "Equipo especializado en resistencia de camellos."
    coach = "Yasmin Al-Rashid"
} -Token $token
Write-Host "  Equipo creado: $($team2.name) -> $($team2.id)"

foreach ($nick in @("Trueno","Relámpago","Ventisca")) {
    Invoke-Api -Method Post -Path "/api/teams/$($team1.id)/members/$($competitors[$nick])" -Token $token | Out-Null
}
foreach ($nick in @("Sahara","Simún")) {
    Invoke-Api -Method Post -Path "/api/teams/$($team2.id)/members/$($competitors[$nick])" -Token $token | Out-Null
}
Write-Host "  Miembros asignados."

# --- Carreras ---
Write-Host "== Creando carreras ==" -ForegroundColor Cyan

$race1 = Invoke-Api -Method Post -Path "/api/races" -Body @{
    name = "Copa de Clasificación EIA"
    description = "Carrera clasificatoria para la temporada 2026."
    scheduledDateTime = "2026-11-15T09:00:00"
    startLocation = "Plaza Central EIA"
    finishLocation = "Cerro El Volador"
    distanceMeters = 5000
    maxParticipants = 10
    raceType = "INDIVIDUAL"
    organizer = "Comité Organizador EIA"
    registrationDeadline = "2026-11-10T23:59:00"
} -Token $token
Write-Host "  Carrera creada (DRAFT): $($race1.name) -> $($race1.id), estado actual: $($race1.raceStatus)"

$race2 = Invoke-Api -Method Post -Path "/api/races" -Body @{
    name = "Sprint de Otoño"
    description = "Carrera corta de velocidad."
    scheduledDateTime = "2026-10-20T10:00:00"
    startLocation = "Parque de las Luces"
    finishLocation = "Universidad EIA"
    distanceMeters = 3000
    maxParticipants = 8
    raceType = "INDIVIDUAL"
    organizer = "Comité Organizador EIA"
    registrationDeadline = "2026-10-15T23:59:00"
} -Token $token
Invoke-Api -Method Patch -Path "/api/races/$($race2.id)/status" -Body @{ status = "OPEN_FOR_REGISTRATION" } -Token $token | Out-Null
Write-Host "  Carrera creada y abierta (OPEN_FOR_REGISTRATION): $($race2.name) -> $($race2.id)"

$race3 = Invoke-Api -Method Post -Path "/api/races" -Body @{
    name = "Gran Premio EIA 2026"
    description = "Carrera principal de la temporada, ya finalizada."
    scheduledDateTime = "2026-09-05T08:00:00"
    startLocation = "Autopista Sur"
    finishLocation = "Estadio EIA"
    distanceMeters = 8000
    maxParticipants = 6
    raceType = "INDIVIDUAL"
    organizer = "Comité Organizador EIA"
    registrationDeadline = "2026-09-01T23:59:00"
} -Token $token
Write-Host "  Carrera creada: $($race3.name) -> $($race3.id)"

Invoke-Api -Method Patch -Path "/api/races/$($race3.id)/status" -Body @{ status = "OPEN_FOR_REGISTRATION" } -Token $token | Out-Null
Write-Host "  -> OPEN_FOR_REGISTRATION"

$participantNicks = @("Trueno", "Relámpago", "Sahara", "Kael")
$registrations = @{}
$pos = 1
foreach ($nick in $participantNicks) {
    $reg = Invoke-Api -Method Post -Path "/api/races/$($race3.id)/registrations" -Body @{
        competitorId = $competitors[$nick]
        startingPosition = $pos
        registeredBy = "admin"
    } -Token $token
    $registrations[$nick] = $reg.id
    Write-Host "  Inscrito: $nick (posición $pos) -> registro $($reg.id)"
    $pos++
}

foreach ($nick in $participantNicks) {
    Invoke-Api -Method Patch -Path "/api/registrations/$($registrations[$nick])/approve" -Token $token | Out-Null
}
Write-Host "  Inscripciones aprobadas."

Invoke-Api -Method Patch -Path "/api/races/$($race3.id)/status" -Body @{ status = "CLOSED_FOR_REGISTRATION" } -Token $token | Out-Null
Write-Host "  -> CLOSED_FOR_REGISTRATION"
Invoke-Api -Method Patch -Path "/api/races/$($race3.id)/status" -Body @{ status = "IN_PROGRESS" } -Token $token | Out-Null
Write-Host "  -> IN_PROGRESS"

Invoke-Api -Method Post -Path "/api/races/$($race3.id)/results" -Body @{
    registrationId = $registrations["Trueno"]
    resultStatus = "FINISHED"
    finalPosition = 1
    completionTimeMillis = 1200000
    penaltyTimeMillis = 0
    notes = "Ganador indiscutido"
    recordedBy = "admin"
} -Token $token | Out-Null

Invoke-Api -Method Post -Path "/api/races/$($race3.id)/results" -Body @{
    registrationId = $registrations["Sahara"]
    resultStatus = "FINISHED"
    finalPosition = 2
    completionTimeMillis = 1260000
    penaltyTimeMillis = 0
    recordedBy = "admin"
} -Token $token | Out-Null

Invoke-Api -Method Post -Path "/api/races/$($race3.id)/results" -Body @{
    registrationId = $registrations["Relámpago"]
    resultStatus = "FINISHED"
    finalPosition = 3
    completionTimeMillis = 1310000
    penaltyTimeMillis = 5000
    notes = "Penalización por salida en falso"
    recordedBy = "admin"
} -Token $token | Out-Null

Invoke-Api -Method Post -Path "/api/races/$($race3.id)/results" -Body @{
    registrationId = $registrations["Kael"]
    resultStatus = "DID_NOT_FINISH"
    penaltyTimeMillis = 0
    notes = "Se retiró por lesión menor"
    recordedBy = "admin"
} -Token $token | Out-Null

Write-Host "  Resultados registrados."

Invoke-Api -Method Patch -Path "/api/races/$($race3.id)/status" -Body @{ status = "COMPLETED" } -Token $token | Out-Null
Write-Host "  -> COMPLETED"

Write-Host ""
Write-Host "== LISTO ==" -ForegroundColor Green
Write-Host "Competidores: $(($dwarfsData + $camelsData + $mediumData).Count) | Equipos: 2 | Carreras: 3 (DRAFT, OPEN_FOR_REGISTRATION, COMPLETED)"
