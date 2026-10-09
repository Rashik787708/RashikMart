/**
 * RashikMart AI Assistant Floating Chat Widget (Spec §11, §17)
 * Safe DOM rendering (textContent only — no innerHTML/XSS), responsive, CSRF protected.
 */
(function() {
    'use strict';

    function getContextPath() {
        const meta = document.querySelector('meta[name="context-path"]');
        if (meta && meta.content) return meta.content;
        const path = window.location.pathname;
        if (path.startsWith('/RashikMart')) return '/RashikMart';
        return '';
    }

    const contextPath = getContextPath();
    const chatEndpoint = contextPath + '/api/v1/chat';

    let csrfToken = '';

    function getCsrfToken() {
        if (csrfToken) return Promise.resolve(csrfToken);
        const meta = document.querySelector('meta[name="csrf-token"]');
        if (meta && meta.content) {
            csrfToken = meta.content;
            return Promise.resolve(csrfToken);
        }
        const input = document.querySelector('input[name="csrfToken"]');
        if (input && input.value) {
            csrfToken = input.value;
            return Promise.resolve(csrfToken);
        }
        // Fetch token from chat GET endpoint
        return fetch(chatEndpoint, { method: 'GET' })
            .then(res => res.json())
            .then(json => {
                if (json && json.data && json.data.csrfToken) {
                    csrfToken = json.data.csrfToken;
                }
                return csrfToken;
            })
            .catch(() => '');
    }

    function initWidget() {
        if (document.getElementById('rm-chat-launcher')) return;

        // Launcher Button
        const launcher = document.createElement('button');
        launcher.id = 'rm-chat-launcher';
        launcher.setAttribute('aria-label', 'Open RashikMart AI Assistant');
        launcher.innerHTML = '<svg viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>';

        // Panel
        const panel = document.createElement('div');
        panel.id = 'rm-chat-panel';
        panel.className = 'rm-hidden';

        // Header
        const header = document.createElement('div');
        header.className = 'rm-chat-header';

        const headerTitle = document.createElement('div');
        headerTitle.className = 'rm-chat-header-title';
        const dot = document.createElement('span');
        dot.className = 'rm-chat-status-dot';
        const titleText = document.createTextNode('RashikMart Assistant');
        headerTitle.appendChild(dot);
        headerTitle.appendChild(titleText);

        const closeBtn = document.createElement('button');
        closeBtn.className = 'rm-chat-close-btn';
        closeBtn.setAttribute('aria-label', 'Close chat');
        closeBtn.textContent = '✕';

        header.appendChild(headerTitle);
        header.appendChild(closeBtn);
        panel.appendChild(header);

        // Messages area
        const messages = document.createElement('div');
        messages.className = 'rm-chat-messages';
        messages.id = 'rm-chat-messages';
        panel.appendChild(messages);

        // Quick Suggestion Chips
        const chipsContainer = document.createElement('div');
        chipsContainer.className = 'rm-chat-chips';
        const sampleQuestions = [
            'How do I order?',
            'How does payment work?',
            'What is the return policy?',
            'Track order status'
        ];
        sampleQuestions.forEach(q => {
            const chip = document.createElement('button');
            chip.className = 'rm-chip';
            chip.type = 'button';
            chip.textContent = q;
            chip.addEventListener('click', () => {
                input.value = q;
                sendMessage();
            });
            chipsContainer.appendChild(chip);
        });
        panel.appendChild(chipsContainer);

        // Input row
        const inputRow = document.createElement('div');
        inputRow.className = 'rm-chat-input-row';

        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'rm-chat-input';
        input.placeholder = 'Ask about products, orders, returns...';
        input.maxLength = 500;

        const sendBtn = document.createElement('button');
        sendBtn.type = 'button';
        sendBtn.className = 'rm-chat-send-btn';
        sendBtn.setAttribute('aria-label', 'Send message');
        sendBtn.innerHTML = '<svg style="width:16px;height:16px;fill:#fff" viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>';

        inputRow.appendChild(input);
        inputRow.appendChild(sendBtn);
        panel.appendChild(inputRow);

        document.body.appendChild(launcher);
        document.body.appendChild(panel);

        // Initial welcome message
        appendMessage('bot', 'Hello! I am your RashikMart shopping assistant. How can I help you today?');

        // Toggle open/close
        launcher.addEventListener('click', () => {
            const isHidden = panel.classList.contains('rm-hidden');
            if (isHidden) {
                panel.classList.remove('rm-hidden');
                input.focus();
                getCsrfToken();
            } else {
                panel.classList.add('rm-hidden');
            }
        });

        closeBtn.addEventListener('click', () => {
            panel.classList.add('rm-hidden');
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendMessage();
            }
        });

        sendBtn.addEventListener('click', (e) => {
            e.preventDefault();
            sendMessage();
        });

        function appendMessage(sender, text) {
            const msgEl = document.createElement('div');
            msgEl.className = 'rm-message rm-message-' + sender;
            msgEl.textContent = text; // Safe text rendering (No innerHTML -> No XSS)
            messages.appendChild(msgEl);
            messages.scrollTop = messages.scrollHeight;
            return msgEl;
        }

        function sendMessage() {
            const text = input.value.trim();
            if (!text) return;

            appendMessage('user', text);
            input.value = '';
            input.disabled = true;
            sendBtn.disabled = true;

            // Loading indicator
            const loadingEl = document.createElement('div');
            loadingEl.className = 'rm-message rm-message-bot rm-chat-loading';
            loadingEl.innerHTML = '<span></span><span></span><span></span>';
            messages.appendChild(loadingEl);
            messages.scrollTop = messages.scrollHeight;

            getCsrfToken().then(token => {
                return fetch(chatEndpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-Token': token
                    },
                    body: JSON.stringify({ message: text })
                });
            })
            .then(res => {
                if (loadingEl.parentNode) loadingEl.parentNode.removeChild(loadingEl);

                if (res.status === 429) {
                    appendMessage('error', 'Rate limit exceeded: Please wait a moment before sending more messages.');
                    return null;
                }
                if (res.status === 403) {
                    appendMessage('error', 'Session verification expired. Please refresh your page.');
                    return null;
                }
                if (!res.ok) {
                    return res.json().then(errData => {
                        const errMsg = (errData && errData.error && errData.error.message)
                            ? errData.error.message
                            : 'Error: Request could not be processed.';
                        appendMessage('error', errMsg);
                        return null;
                    }).catch(() => {
                        appendMessage('error', 'Service temporarily unavailable. Please try again.');
                        return null;
                    });
                }
                return res.json();
            })
            .then(json => {
                if (json && json.data && json.data.reply) {
                    appendMessage('bot', json.data.reply);
                }
            })
            .catch(err => {
                if (loadingEl.parentNode) loadingEl.parentNode.removeChild(loadingEl);
                appendMessage('error', 'Network error. Please check your connection.');
            })
            .finally(() => {
                input.disabled = false;
                sendBtn.disabled = false;
                input.focus();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initWidget);
    } else {
        initWidget();
    }
})();
