const chatState = {
    sessionId: crypto.randomUUID(),
    isWaiting: false //Flag for waiting for response, to prevent multiple sends
};

//Save all HTML-elements to reduce boilerplate
const dom = {
    personality: document.getElementById('personality-select'),
    input: document.getElementById('user-input'),
    button: document.getElementById('send-button'),
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
            try {
                const errorData = await response.json(); //Read JSON-error from @ControllerAdvice
                throw new Error(errorData.message || 'The Gods are Silent');
            } catch (jsonError) {
                //Fallback if not json
                throw new Error("Divine connection lost", { cause: jsonError });
            }
        }

        const aiText = await response.text(); //If ok, read response as text from ai
        appendMessage('assistant', godName, aiText);
    } catch (error) {
        appendMessage('assistant', 'System', error.message);
    } finally {
        setLoading(false); //Hide loading indicator weather success or not
    }
}

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
        msgDiv.appendChild(document.createTextNode(text));
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