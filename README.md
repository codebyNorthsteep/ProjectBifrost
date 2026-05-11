# 🌉 Asgard AI - Speak to the Gods
Välkommen du modige äventyrare! 
ProjectBifrost(Asgard AI) är en modern chatt-applikation där du kan kommunicera med fem olika nordiska gudar, var och en med sin egen unika personlighet. Systemet använder avancerad prompt-engineering och AI via OpenRouter för att ge autentiska svar.
## 🎯 The Powers of Asgard

- 🗣️ **Fem gudomliga personligheter**: Odin, Loki, Thor, Freyja, och Heimdall, var och en med unika svarsstilar och personlighetsdrag (Prompt Engineering)
- ✉️ **Skicka meddelande**: Skriv ett meddelande och klicka "Send" eller tryck Enter. Systemet skickar en unik session ID, ditt meddelande, och den valda gudens personlighet till backend.
- 💬 **Chatthistorik**: Dina samtal sparas server-side och hämtas automatiskt vid sidomladdning via ditt sessions ID. Persistent chat-historik per session.
- 🗑️ **Clear Chat**: Rensa all chatthistorik med en knapp, återställer personlighetsval och visar bekräftelseruta
- 🛡️ **Resilience**: Circuit Breaker + Retry-logik för robust API-hantering
- ♿ **Accessible**: Nordisk-inspirerad design med responsiv layout och gud-chips för snabba val.
- ⚡ **Real-time Feedback**: Visar när gudarna "tänker" (typing indicator) och hanterar timeouts snyggt.
- 🎨 **Tema**: Nordisk-inspirerad design med guld och mörkblå nyanser

## 🎬 Live Demo

Se hur smidigt det går att konversera med Asgårds gudar! Notera typing-indicatorn när guden förbereder sitt svar. **Very demure, very mindful** 

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

Standard-modellen är `poolside/laguna-xs.2:free` (gratis). Du kan byta till en annan modell genom att redigera `src/main/resources/application.properties` till exempelvis:

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
| DELETE | `/api/v1/chat/{sessionId}` | Rensa chat-historik |

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
## 🗂️ Backend

- **Personality Enum**: Varje gud har en unik system prompt som injiceras tillsammans med meddelandena för att ge rätt personlighet
- **ChatSessionStorage**: In-memory cache med ConcurrentHashMap som sparar alla sessioner och deras meddelandehistorik
- **GlobalExceptionHandler**: Centraliserad felhantering med `@ControllerAdvice` som översätter exceptions till tematiska felmeddelanden (t.ex. "The Gods are silent" för service unavailable). Hanterar validering, timeouts, LLM-fel och malformad JSON
- **Message Flow**: POST `/api/v1/chat` validera → hämta/skapa session → bygg prompt-lista [system prompt + historik + nytt meddelande] → kalla LLM via OpenRouter → spara svar i session → returnera till frontend

**Resilience:**
- 🔄 **Retry**: Max 3 försök med exponential backoff
- 🚫 **Circuit Breaker**: Öppnas efter 50% fel-rate på 10 samtal

## 🧠 Frontend (JavaScript)

- 🪟 **Din väg över Bifrost**: Session ID genereras automatiskt via `crypto.randomUUID()` och sparas i `localStorage.bifrost_session_id`, så länge du inte tömmer webbläsarens cache
- ⚓ **Heimdall Vaktar**: Default personlighet sätts i HTML och JS (`dom.personality.value = 'HEIMDALL'`)
- ✉️ **Skicka meddelande**: JavaScript använder `fetch()` POST till `/api/v1/chat` med AbortController för 15-sekunders timeout. Vid error visas tematiskt meddelande från systemet (t.ex. "The Gods took too long to respond...")
- 📜 **Runor från Mnemosyne - Minnets gudinna**:
  - Alla sessioner med meddelanden sparas **lokalt** i `ChatSessionStorage` (in-memory)
  - Vid sidladdning hämtar JavaScript automatiskt samtida historik via `GET /api/v1/chat/{sessionId}`
  - Om du laddar om sidan → samma session ID → all historik visas igen
  - Sessions försvinner endast när Java-servern omstartas (in-memory)
- 🗑️ **Clear-knappen**: Rensar chatthistorik för sessionen via `DELETE /api/v1/chat/{sessionId}`, återställer personlighetsvalet till Heimdall, och visar bekräftelseruta före borttagning
- 🛡️ **XSS Protection**: All AI-genererad Markdown tvättas via DOMPurify innan rendering för att förhindra skadlig kodinjektion.

---

## 🛠️ Techstack

- **Backend**: Spring Boot 4.0.6, Java 25
- **Frontend**: Vanilla JavaScript, Marked.js (Markdown parsing för snabb & snygg formatering av meddelanden), HTML, CSS
- **API**: OpenRouter (LLM)
- **Resilience**: Resilience4j
- **Testning**: JUnit 5, AssertJ, Mockito, WireMock
- **Dokumentation**: Springdoc OpenAPI(Swagger)

---

## 🧪 Kör tester

```bash
./mvnw test
```

Testsvit inkluderar enhetstester för storage, service, och controllerns endpoints

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
