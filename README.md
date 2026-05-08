# 🌉 ProjectBifrost - Speak to the Gods
Välkommen du modige äventyrare! 
ProjectBifrost är en modern chatt-applikation där du kan kommunicera med fem olika nordiska gudar, var och en med sin egen unika personlighet. Systemet använder avancerad prompt-engineering och AI via OpenRouter för att ge autentiska svar.
## 🎯 Features

- 🗣️ **Fem gudomliga personligheter**: Odin, Loki, Thor, Freyja, och Heimdall
- 💬 **Chathistorik**: Dina samtal sparas lokalt i webbläsaren och hämtas automatiskt vid sidomladdning.
- 🛡️ **Resilience**: Circuit Breaker + Retry-logik för robust API-hantering
- ♿ **Accessible**: Nordisk-inspirerad design med responsiv layout och gud-chips för snabba val.
- ⚡ **Real-time Feedback**: Visar när gudarna "tänker" (typing indicator) och hanterar timeouts snyggt.
- 🎨 **Tema**: Nordisk-inspirerad design med guld och mörkblå nyanser

## 🎬 Live Demo

Se hur smidigt det går att konversera med Asgårds gudar! Notera typing-indicatorn när guden förbereder sitt svar. **Very demure** 

<img width="534" height="498" alt="finalGifDemo" src="https://github.com/user-attachments/assets/46a9c2eb-c061-4967-aec6-99e5b6739172" />

---

## ⚡ Snabbstart

### 1️⃣ Klona och bygg

```bash
git clone <repo-url>
cd ProjectBifrost
./mvnw clean install
```

### 2️⃣ Hämta OpenRouter API-nyckel

1. Gå till https://openrouter.ai/
2. Klicka **"Sign up"** och skapa ett konto
3. Gå till **Settings → API Keys**
4. Klicka **"Create new token"**
5. Kopiera din API-nyckel (den börjar ofta med `sk-`)

⚠️ **Spara denna nyckel någonstans säker!** Du kommer bara se den en gång.

### 3️⃣ Ställ in miljövariabler

#### **Windows (PowerShell):**
```powershell
$env:OPENROUTER_API_KEY="sk-ditt-hemliga-nyckel-här"
```

#### **Windows (Command Prompt):**
```cmd
set OPENROUTER_API_KEY=sk-ditt-hemliga-nyckel-här
```

#### **Mac/Linux (Bash/Zsh):**
```bash
export OPENROUTER_API_KEY="sk-ditt-hemliga-nyckel-här"
```

#### **Permanent (alla OS - lägg i `.env` eller `.bashrc`):**
```
OPENROUTER_API_KEY=sk-ditt-hemliga-nyckel-här
```

### 3.5️⃣ (Valfritt) Välj en annan LLM-modell

Standard-modellen är `poolside/laguna-xs.2:free` (gratis). Du kan byta till en annan modell genom att redigera `src/main/resources/application.properties`:

```properties
openrouter.model=gpt-4o-mini
```
Se alla tillgängliga modeller på: https://openrouter.ai/models

⚠️ Vissa modeller kostar pengar, kolla prisen innan du byter!

### 4️⃣ Starta applikationen

```bash
./mvnw spring-boot:run
```

Öppna http://localhost:8080 i din webbläsare 🌐

---

## 📚 API & Swagger

### 🔍 Swagger UI
Endpoints och API-dokumentation finns på:

```
http://localhost:8080/swagger-ui.html
```

Där kan du se:
- ✅ Alla endpoints
- 📝 Request/Response exempel
- 🧪 Testa endpoints direkt

### 📡 Huvudsakliga endpoints

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| POST | `/api/v1/chat` | Skicka meddelande till gud |
| GET | `/api/v1/chat/{sessionId}` | Hämta chat-historik |

---

## 🏗️ Arkitektur

```
BifrostController (REST)
    ↓
ChatService (Affärslogik + Resilience4j)
    ↓
RestClient (OpenRouter API)
    ↓
ChatSessionStorage (In-memory cache)
```

**Resilience:**
- 🔄 **Retry**: Max 3 försök med exponential backoff
- 🚫 **Circuit Breaker**: Öppnas efter 50% fel-rate på 10 samtal

---

## 🧠 Frontend (JavaScript)

- 🪟 **Din väg över Bifrost**: Session ID genereras automatiskt via `crypto.randomUUID()` och sparas i `localStorage.bifrost_session_id`, så länge du inte tömmer webbläsarens cache
- ⚓ **Heimdall Vaktar**: Default personlighet sätts i HTML och JS (`dom.personality.value = 'HEIMDALL'`)
- 📜 **Runor från Mnemosyne - Minnets gudinna**:
  - Alla sessioner med meddelanden sparas **lokalt** i `ChatSessionStorage` (in-memory)
  - Vid sidladdning hämtar JavaScript automatiskt samtida historik via `GET /api/v1/chat/{sessionId}`
  - Om du laddar om sidan → samma session ID → all historik visas igen
  - Sessions försvinner endast när Java-servern omstartas (in-memory)

---

## 🛠️ Techstack

- **Backend**: Spring Boot 4.0.6, Java 25
- **Frontend**: Vanilla JavaScript, HTML, CSS
- **API**: OpenRouter (LLM)
- **Resilience**: Resilience4j
- **Testning**: JUnit 5, AssertJ, Mockito, WireMock
- **Dokumentation**: Springdoc OpenAPI(Swagger)

---

## 🧪 Kör tester

```bash
./mvnw test
```

---

## ⚠️ Viktigt

- Du behöver ett giltigt OpenRouter API-nyckel för att chatten ska fungera
- Se till att du angett din `OPENROUTER_API_KEY` innan du startar appen
- Sessions sparas i **minnet** och försvinner vid omstart av applikationen

---

## 📝 Licens

Fritt att använda för hobby/lärande

---

**Gjord med ❤️ för nordisk mytologi och AI** ⚡🗡️
