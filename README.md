# Documentație Proiect: Sistem de Partajare a Fișierelor P2P (Peer-to-Peer)

**Disciplina:** Tehnologii de Programare Java pentru Aplicații Distribuite (TPJAD)  
**Student:** Marcu Bogdan  
**Data:** 10 Ianuarie 2026

---

## 1. Introducere

### 1.1. Obiectivul Proiectului
Scopul acestui proiect este dezvoltarea unui sistem distribuit de stocare și partajare a fișierelor, bazat pe o arhitectură hibridă Microservicii + Peer-to-Peer (P2P). Sistemul permite utilizatorilor autentificați să încarce fișiere, care sunt ulterior fragmentate ("sharded"), replicate și distribuite pe mai multe noduri dintr-un cluster, asigurând redundanță și disponibilitate.

### 1.2. Scopul Aplicației
Aplicația rezolvă problema punctului unic de eșec (Single Point of Failure) în stocarea fișierelor prin descentralizarea datelor. De asemenea, separă metadatele (gestionate centralizat) de conținutul efectiv (gestionat distribuit), optimizând performanța și scalabilitatea.

---

## 2. Arhitectura Sistemului

Sistemul este compus din trei module principale care interacționează sincron (HTTP/gRPC) și asincron (Kafka).

### 2.1. Componente Principale

1.  **p2pFileTracker (Tracker Service):**
    *   **Rol:** Gestionează metadatele fișierelor (nume, proprietar, descriere) și autentificarea.
    *   **Funcționalitate:** Nu stochează fișierele pe disc. Primește fișierul, salvează metadatele în baza de date PostgreSQL, convertește conținutul în Base64 și îl trimite asincron către coada Kafka.
2.  **p2pBackend (Storage Node):**
    *   **Rol:** Nod de stocare și procesare din clusterul P2P.
    *   **Funcționalitate:**
        *   Consumă mesaje din Kafka.
        *   Fragmentează fișierul în 4 părți (shards).
        *   Distribuie fragmentele către alte noduri folosind gRPC.
        *   Reconstruiește fișierele la cerere prin agregarea fragmentelor locale și de la distanță.
        *   Participă la protocolul de descoperire a nodurilor (Gossip).
3.  **web-client (Frontend):**
    *   **Rol:** Interfața cu utilizatorul.
    *   **Funcționalitate:** Permite vizualizarea fișierelor, încărcarea, ștergerea și descărcarea acestora. Comunică direct cu nodurile P2P pentru transferul de date.

### 2.2. Diagrama de Flux a Datelor

1.  **Upload:** Frontend -> Tracker (HTTP) -> Kafka (Payload) -> Backend (Consumer) -> Sharding -> Distribuție (gRPC) -> Disk.
2.  **Download:** Frontend -> Backend (HTTP) -> Reconstrucție (Local + gRPC de la alți colegi) -> Frontend.
3.  **Delete:** Frontend -> Tracker -> Kafka -> Backend -> Ștergere Locală + Broadcast (gRPC).

### 2.3. Infrastructură și Tehnologii
*   **Limbaj:** Java 21 (Backend), JavaScript/React (Frontend).
*   **Frameworks:** Spring Boot 3.4 (Web, Data JPA, Kafka, Scheduling), React 19.
*   **Comunicare:** REST API, Apache Kafka, gRPC (Google Protocol Buffers).
*   **Baze de Date:** PostgreSQL (Metadate).
*   **Securitate (Identity & Access Management):** Keycloak.
    *   **Rol:** Server de autorizare centralizat.
    *   **Realm:** `P2PFileShare` (domeniu dedicat aplicației).
    *   **Client:** `web-client` (Public client pentru React) și `tracker-service` (Bearer-only/Confidential pentru validare tokeni).
    *   **Tokeni:** JWT (JSON Web Tokens) care conțin informațiile utilizatorului (sub, username, email, roles).
*   **Containerizare:** Docker & Docker Compose.

---

## 3. Descrierea Implementării (Nivel de Funcționalitate)

### 3.1. Modulul `p2pFileTracker`

Acesta este punctul de intrare pentru operațiunile de scriere.

*   **`TrackerController.java`**
    *   `uploadFile(FileUploadRequestDTO)`: Endpoint POST. Primește fișierul și metadatele. Validează token-ul JWT. Deleagă către serviciu.
    *   `getManifests()`: Returnează lista fișierelor publice.
    *   `getUserManifests()`: Returnează fișierile utilizatorului curent.
*   **`TrackerService.java`**
    *   `uploadFile()`:
        1.  Salvează entitatea `ManifestEntity` în PostgreSQL.
        2.  Creează un `KafkaFilePayloadDTO` cu conținutul fișierului.
        3.  Trimite mesajul pe topicul `p2p-file-uploads`.
    *   `deleteManifest()`: Șterge intrarea din DB și emite un eveniment pe topicul `p2p-file-deletes`.

### 3.2. Modulul `p2pBackend`

Acesta este "creierul" distribuit. Poate rula în multiple instanțe.

#### A. Procesarea și Stocarea (Ingestion)
*   **`KafkaConfiguration.java`**: Configurat să folosească `JsonDeserializer` mapat pe `Object` pentru a evita erorile de serializare între module diferite. S-a setat limita de mesaj la 20MB.
*   **`FileIngestionService.java`**
    *   `listen(ConsumerRecord)`: Metoda declanșată automat la primirea unui mesaj Kafka.
        1.  Decodifică fișierul din Base64.
        2.  Împarte fișierul în 4 fragmente egale (`shard`).
        3.  Pentru fiecare fragment, selectează 2 noduri aleatoare din cluster (inclusiv sine).
        4.  Apelează `distributeShard` care trimite datele prin gRPC (metoda `storeShard`) către nodurile selectate.

#### B. Comunicarea între Noduri (Cluster & gRPC)
*   **`p2p.proto`**: Definește contractul gRPC: `StoreShard`, `GetShard`, `DeleteShard`.
*   **`ClusterService.java`**
    *   `gossip()`: Executat la fiecare 5 secunde. Alege un nod aleator și trimite lista proprie de noduri cunoscute. Asigură convergența clusterului.
    *   `joinViaBootstrap()`: La pornire, contactează un nod "bootstrap" cunoscut pentru a intra în rețea.
    *   `getPeerStub(Node)`: Creează un canal gRPC către un alt nod.
*   **`ShardServiceImpl.java`**: Implementarea serverului gRPC.
    *   `storeShard`: Primește un fragment de la alt nod și îl scrie pe disc via `ShardStorageService`.
    *   `getShard`: Citește un fragment de pe disc și îl returnează solicitantului.

#### C. Recuperarea Fișierelor (Retrieval)
*   **`FileRetrievalService.java`**
    *   `retrieveAndReconstruct(fileId)`:
        1.  Iterează de la indexul 0 la 3 (cele 4 fragmente).
        2.  Verifică dacă fragmentul există local pe disc.
        3.  Dacă nu, iterează prin lista de `peers` și face request-uri gRPC (`GetShard`) până găsește fragmentul.
        4.  Concatenează fragmentele într-un `ByteArrayOutputStream` și returnează fișierul complet.
*   **`NodeController.java`**
    *   `downloadFile()`: Endpoint HTTP GET. Folosește serviciul de mai sus pentru a reconstrui fișierul și îl servește ca `application/octet-stream` browserului.

### 3.3. Modulul `web-client`

*   **`P2PService.js`**: Implementează logica de Load Balancing pe partea de client (Client-Side Discovery).
    *   `discoverAndSelectPeer()`: Interoghează nodul bootstrap pentru lista de noduri active.
    *   `setCookie()`: Salvează adresa unui nod activ în cookie pentru a menține sesiunea de download pe același nod ("sticky session" simplificat).
    *   `downloadFile()`: Dacă descărcarea eșuează de pe nodul curent, șterge cookie-ul și caută alt nod.

---

## 4. Precizarea Intrărilor și Ieșirilor

| Flux | Intrare (Input) | Procesare | Ieșire (Output) |
| :--- | :--- | :--- | :--- |
| **Upload** | Fișier (binar), Metadate (Nume, Descriere, Vizibilitate) | Salvare DB, Serializare Kafka, Sharding, Replicare gRPC | Confirmare JSON (Manifest), 4 fișiere `.json` pe disc (distribuite) |
| **Listare** | Token JWT (User) | Interogare DB (Postgres) | Lista JSON cu metadate fișiere |
| **Download** | ID Fișier, Token JWT | Căutare fragmente (Local + Rețea), Concatenare | Fișier binar (Blob) descărcabil în browser |
| **Ștergere** | ID Fișier | Ștergere DB, Propagare eveniment Kafka, RPC `DeleteShard` | Confirmare ștergere, dispariția fișierelor de pe disc |

---

## 5. Structura de Directoare și Fișiere

Mai jos este prezentată structura directoarelor, evidențiind fișierele sursă critice implementate.

```text
/
├── p2pFileTracker/ (Spring Boot - Metadata & Producer)
│   ├── src/main/java/.../p2pfiletracker/
│   │   ├── configurations/
│   │   │   └── KafkaConfiguration.java (Setări Producer, max.request.size 20MB)
│   │   ├── controllers/
│   │   │   └── TrackerController.java (REST API pentru Upload/Listare)
│   │   ├── entities/
│   │   │   └── ManifestEntity.java (Mapare tabelă PostgreSQL)
│   │   └── services/
│   │       ├── TrackerService.java (Logica de business, trimitere Kafka)
│   │       └── DownloadTrackingService.java (Consumator evenimente download)
│   └── src/main/resources/application.yml
│
├── p2pBackend/ (Spring Boot - Storage Node)
│   ├── src/main/java/.../p2pbackend/
│   │   ├── configurations/
│   │   │   └── KafkaConfiguration.java (Setări Consumer deserializare Object)
│   │   ├── controllers/
│   │   │   ├── ClusterController.java (Status cluster, Gossip)
│   │   │   └── NodeController.java (Endpoint Download)
│   │   ├── services/
│   │   │   ├── ClusterService.java (Management membri cluster, gRPC Stubs)
│   │   │   ├── FileIngestionService.java (Sharding, Distribuție)
│   │   │   ├── FileRetrievalService.java (Reconstrucție fișier)
│   │   │   ├── ShardStorageService.java (Scriere/Citire disc)
│   │   │   ├── ShardServiceImpl.java (Server gRPC)
│   │   │   └── FileDeletionService.java (Coordonare ștergere)
│   │   └── models/ (Node, GossipRequest)
│   ├── src/main/proto/
│   │   └── p2p.proto (Definiția serviciilor gRPC)
│   └── start_cluster.sh (Script Bash pentru pornirea a 3 noduri locale)
│
├── web-client/ (React)
│   ├── src/
│   │   ├── components/
│   │   │   ├── EditManifestModal.js (Modal editare)
│   │   │   └── NodeStatus.js (Indicator noduri active)
│   │   ├── services/
│   │   │   ├── TrackerService.js (API Metadate)
│   │   │   └── P2PService.js (Logica Smart Download & Cookies)
│   │   ├── views/
│   │   │   ├── UploadManifestPage.js
│   │   │   ├── BrowseManifestsPage.js
│   │   │   └── ProfilePage.js (Management fișiere proprii)
│   │   └── styles/ (Fișiere CSS)
│   └── package.json
│
└── database/
    └── docker-compose.yml (Definire Postgres, Zookeeper, Kafka)
```

---

## 6. Configurare și Rulare

Pentru a rula sistemul complet pe o mașină locală, se utilizează următoarele comenzi:

1.  **Pornire Infrastructură:**
    ```bash
    cd database
    docker-compose up -d
    ```
    *Aceasta pornește containerele pentru Postgres, Zookeeper și Kafka (configurat pentru mesaje mari).*

2.  **Pornire Keycloak:**
    *   Necesită server Keycloak separat rulând pe portul 8180 (realm `P2PFileShare`).

3.  **Pornire Tracker:**
    ```bash
    cd p2pFileTracker
    ./mvnw spring-boot:run
    ```

4.  **Pornire Cluster P2P (Backend):**
    ```bash
    cd p2pBackend
    chmod +x start_cluster.sh
    ./start_cluster.sh
    ```
    *Acest script compilează aplicația și lansează 3 procese Java distincte pe porturile 5555, 5556, 5557, simulând un cluster.*

5.  **Pornire Client Web:**
    ```bash
    cd web-client
    npm start
    ```

---

## 7. Concluzii

Proiectul demonstrează implementarea cu succes a unui sistem distribuit complex. Arhitectura aleasă permite scalarea orizontală a stocării prin adăugarea de noi noduri în clusterul P2P, fără a afecta serviciul de metadate. Utilizarea protocolului gRPC asigură o comunicare rapidă și eficientă între noduri, iar Kafka decuplează procesul de încărcare de cel de procesare, oferind reziliență la vârfuri de sarcină. Interfața React oferă o experiență fluidă, gestionând inteligent conexiunile către nodurile distribuite.
