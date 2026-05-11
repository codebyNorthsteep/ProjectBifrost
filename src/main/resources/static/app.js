const getSessionId = () => {
    let id = localStorage.getItem('bifrost_session_id'); //Try to find the old sessionID or create a new one
    if (!id) {
        id = crypto.randomUUID();
        localStorage.setItem('bifrost_session_id', id);
    }
    return id;
};

const chatState = {
    sessionId: getSessionId(),
    isWaiting: false
};

//Save all HTML-elements to reduce boilerplate
const dom = {
    personality: document.getElementById('personality-select'),
    input: document.getElementById('user-input'),
    button: document.getElementById('send-button'),
    clearBtn: document.getElementById('clear-button'),
    window: document.getElementById('chat-window'),
    typing: document.getElementById('typing-indicator'),
    chips: document.querySelectorAll('.god-chip')
};

// ── God chip selection ──────────────────────────────────────────

dom.chips.forEach(chip => {
    chip.addEventListener('click', () => {
        //Remove "active" from all chips
        dom.chips.forEach(c => c.classList.remove('active'));
        //Add "active" to the chip klicked on
        chip.classList.add('active');
        dom.personality.value = chip.dataset.god; //
    });
});

// Set Heimdall as default active chip on load
const defaultChip = document.querySelector('.god-chip[data-god="HEIMDALL"]');
if (defaultChip) defaultChip.classList.add('active');
dom.personality.value = 'HEIMDALL';

// ── Chat ────────────────────────────────────────────────────────

async function sendMessage() {
    const message = dom.input.value.trim();
    if (!message || chatState.isWaiting) return;

    const selectedPersonality = dom.personality.value;
    const godName = getGodName(selectedPersonality);

    appendMessage('user', null, message);
    dom.input.value = '';
    setLoading(true); //Show text from HTML in waiting for response

    try {
        //AbortController, set a timer for 15 sek while waiting for response
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 15000);
        let response;

        try {
            response = await fetch('/api/v1/chat', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    personality: selectedPersonality,
                    message: message,
                    sessionId: chatState.sessionId //Randomized id
                }),
                signal: controller.signal
            });
        } finally {
            clearTimeout(timeoutId); //If a response was given, turn of timer
        }

        if (!response.ok) {
            let errorMessage = 'The Gods are Silent';
            try {
                const errorData = await response.json(); //Read JSON-error from @ControllerAdvice
                errorMessage = errorData.message || errorMessage;
            } catch {
                errorMessage = 'Divine connection lost';
            }
            throw new Error(errorMessage);
        }

        const aiText = await response.text(); //If ok, read response as text from ai
        appendMessage('assistant', godName, aiText);
    } catch (error) {
        //Themed timeout message
        const errorMsg = error.name === 'AbortError'
            ? 'The Gods took too long to respond...'
            : error.message;
        appendMessage('assistant', 'System', errorMsg);
    } finally {
        setLoading(false); //Hide loading indicator weather success or not
    }
}

async function clearChat() {
    if (chatState.isWaiting) return; //Block clearing if waiting for response, race conditions could occur
    if (!confirm("Are you sure you want to clear your chat history?")) return;

    try {
        const response = await fetch(`/api/v1/chat/${chatState.sessionId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            dom.window.innerHTML = '';

            //Reset to default chip selection
            dom.chips.forEach(c => c.classList.remove('active'));
            const defaultChip = document.querySelector('.god-chip[data-god="HEIMDALL"]');
            if (defaultChip) defaultChip.classList.add('active');
            dom.personality.value = 'HEIMDALL';
            appendMessage('assistant', 'Heimdall', 'The runes are cast anew. The gods have forgotten your past whispers. A clean slate at the foot of Yggdrasil. Pick which god shall lead your path this time?');
        }
    } catch (err) {
        console.error("Could not clear chat history:", err);
    }
}

// Koppla knappen till funktionen
dom.clearBtn.addEventListener('click', clearChat);

// ── UI helpers ────────────────────────────────────────────────────────

//Display and format messages in chat window, with different styling for user and assistant. Also scrolls to bottom when new message is added
function appendMessage(role, name, text) {
    const msgDiv = document.createElement('div');//Create a new HTML element in memory
    msgDiv.className = `message ${role}`;

    if (role === 'assistant') {
        const label = document.createElement('div');
        label.className = 'god-label';
        label.textContent = name;
        msgDiv.appendChild(label);
        const contentDiv = document.createElement('div');
        contentDiv.className = 'markdown-body';
        //Render markdown to HTML
        const rawHtml = window.marked.parse(text);
        //Sanitize the HTML to prevent XSS attacks, then set it as content of the message
        contentDiv.innerHTML = DOMPurify.sanitize(rawHtml);
        msgDiv.appendChild(contentDiv);
    } else {
        msgDiv.textContent = text;
    }

    dom.window.appendChild(msgDiv);
    dom.window.scrollTop = dom.window.scrollHeight;
}

function setLoading(active) {
    chatState.isWaiting = active;
    dom.typing.classList.toggle('hidden', !active);
    dom.button.disabled = active;

    //Deactivate clear button while waiting for response, to prevent race conditions
    if (dom.clearBtn) {
        dom.clearBtn.disabled = active;
        dom.clearBtn.style.opacity = active ? "0.5" : "1"; // Valfritt: gör den lite genomskinlig
        dom.clearBtn.style.cursor = active ? "not-allowed" : "pointer";
    }
}

function getGodName(value) {
    const names = {
        ODIN: 'Odin',
        LOKI: 'Loki',
        THOR: 'Thor',
        FREYJA: 'Freyja',
        HEIMDALL: 'Heimdall'
    };
    return names[value] || value;
}

// ── Event listeners ─────────────────────────────────────────────

dom.button.addEventListener('click', sendMessage);
dom.input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage().then(r => {
        }).catch(err => console.error(err));
    }
});

// Körs automatiskt när sidan laddas om
window.addEventListener('DOMContentLoaded', async () => {
    try {
        // Vi anropar din befintliga GetMapping: /api/v1/chat/{sessionId}
        const response = await fetch(`/api/v1/chat/${chatState.sessionId}`);

        if (response.ok) {
            const sessionData = await response.json(); // Detta matchar din ChatSession-klass

            // if there is sessionData.chatHistory saved, we loop through it and display the messages in the chat window
            if (sessionData && sessionData.chatHistory && sessionData.chatHistory.length > 0) {
                sessionData.chatHistory.forEach(msg => {
                    const role = msg.role === 'assistant' ? 'assistant' : 'user';
                    // Name saved from backend
                    const name = role === 'assistant' ? getGodName(msg.senderName) : null;

                    appendMessage(role, name, msg.content);
                });
            }
        }
    } catch (err) {
        console.error("Could not fetch chat history:", err);
    }
});
