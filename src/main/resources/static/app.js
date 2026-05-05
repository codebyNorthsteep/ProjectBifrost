const chatState = {
    sessionId: crypto.randomUUID(),
    isWaiting: false
};

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
        dom.chips.forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        dom.personality.value = chip.dataset.god;
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
    setLoading(true);

    try {
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
                    sessionId: chatState.sessionId
                }),
                signal: controller.signal
            });
        } finally {
            clearTimeout(timeoutId);
        }

        if (!response.ok) throw new Error("Divine connection lost.");

        const aiText = await response.text();
        appendMessage('assistant', godName, aiText);
    } catch (error) {
        appendMessage('assistant', 'System', "Error: " + error.message);
    } finally {
        setLoading(false);
    }
}

function appendMessage(role, name, text) {
    const msgDiv = document.createElement('div');
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