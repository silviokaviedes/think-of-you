let token = localStorage.getItem('token');
let currentUsername = localStorage.getItem('username');
let stompClient = null;
let currentStatsConnectionId = null;
let chart = null;
let selectedMoods = {};

// Initialization
if (token) {
    showDashboard();
    connectWebSocket();
}

// UI Helpers
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    
    if (type !== 'info' || message !== 'Loading statistics...') {
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
    return toast;
}

// UI Navigation
function setLoading(loading) {
    const btns = document.querySelectorAll('button');
    btns.forEach(b => {
        if (loading) {
            b.disabled = true;
            b.style.opacity = '0.7';
        } else {
            b.disabled = false;
            b.style.opacity = '1';
        }
    });
}

function showDashboard() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('dashboard-section').classList.remove('hidden');
    document.getElementById('search-section').classList.add('hidden');
    document.getElementById('stats-section').classList.add('hidden');
    document.getElementById('user-info').classList.remove('hidden');
    document.getElementById('current-username').innerText = currentUsername;
    loadPartners();
    loadRequests();
}

function showSearch() {
    document.getElementById('dashboard-section').classList.add('hidden');
    document.getElementById('search-section').classList.remove('hidden');
    document.getElementById('search-result').innerHTML = '';
}

function showStats(connectionId, partnerName) {
    currentStatsConnectionId = connectionId;
    document.getElementById('dashboard-section').classList.add('hidden');
    document.getElementById('stats-section').classList.remove('hidden');
    document.getElementById('stats-title').innerText = `Statistics for ${partnerName}`;
    loadStats();
}

// Auth Functions
async function login() {
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;
    if (!user || !pass) return showToast('Please enter credentials', 'error');
    
    setLoading(true);
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });
        if (res.ok) {
            const data = await res.json();
            token = data.token;
            currentUsername = data.username;
            localStorage.setItem('token', token);
            localStorage.setItem('username', currentUsername);
            showDashboard();
            connectWebSocket();
            showToast(`Welcome back, ${currentUsername}!`, 'success');
        } else {
            showToast('Login failed. Please check your credentials.', 'error');
        }
    } catch (e) { 
        console.error(e);
        showToast('Connection error', 'error');
    } finally {
        setLoading(false);
    }
}

async function register() {
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;
    if (!user || !pass) return showToast('Please enter credentials', 'error');

    setLoading(true);
    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });
        if (res.ok) {
            showToast('Registered successfully! You can now login.', 'success');
        } else {
            showToast('Registration failed. Username might be taken.', 'error');
        }
    } catch (e) { 
        console.error(e);
        showToast('Connection error', 'error');
    } finally {
        setLoading(false);
    }
}

function logout() {
    localStorage.clear();
    location.reload();
}

// Partner Management
async function loadPartners() {
    const res = await fetch('/api/connections', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const partners = await res.json();
    console.log('Loaded partners:', partners); // Debug log
    const list = document.getElementById('partners-list');
    list.innerHTML = '';
    
    if (partners.length === 0) {
        document.getElementById('no-partners').classList.remove('hidden');
    } else {
        document.getElementById('no-partners').classList.add('hidden');
        partners.forEach(p => {
            console.log(`Partner ${p.partnerUsername} last mood:`, p.lastReceivedMood); // Debug log
            const card = document.createElement('div');
            card.className = 'partner-card';
            card.innerHTML = `
                <h3>${p.partnerUsername}</h3>
                <div class="stats-grid">
                    <div class="stat-item">
                        <span class="stat-label">Received</span>
                        <span class="stat-value" id="received-${p.id}">${p.receivedClicks}</span>
                        ${p.lastReceivedMood && p.lastReceivedMood !== 'none' && p.lastReceivedMood !== 'NONE' ? `<span class="last-mood">${getMoodEmoji(p.lastReceivedMood)}</span>` : ''}
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Sent</span>
                        <span class="stat-value" id="sent-${p.id}">${p.sentClicks}</span>
                    </div>
                </div>
                <div class="mood-selector" id="mood-selector-${p.id}">
                    <label class="mood-label">How are you feeling?</label>
                    <div class="mood-options">
                        <button class="mood-btn" data-mood="happy" data-connection="${p.id}" onclick="selectMood('happy', '${p.id}')">😊 Happy</button>
                        <button class="mood-btn" data-mood="love" data-connection="${p.id}" onclick="selectMood('love', '${p.id}')">❤️ Love</button>
                        <button class="mood-btn" data-mood="sad" data-connection="${p.id}" onclick="selectMood('sad', '${p.id}')">😢 Sad</button>
                        <button class="mood-btn" data-mood="angry" data-connection="${p.id}" onclick="selectMood('angry', '${p.id}')">😠 Angry</button>
                        <button class="mood-btn" data-mood="excited" data-connection="${p.id}" onclick="selectMood('excited', '${p.id}')">🤗 Excited</button>
                        <button class="mood-btn" data-mood="worried" data-connection="${p.id}" onclick="selectMood('worried', '${p.id}')">😟 Worried</button>
                        <button class="mood-btn" data-mood="grateful" data-connection="${p.id}" onclick="selectMood('grateful', '${p.id}')">🙏 Grateful</button>
                        <button class="mood-btn selected" data-mood="none" data-connection="${p.id}" onclick="selectMood('none', '${p.id}')">💭 Neutral</button>
                    </div>
                </div>
                <button class="think-btn" onclick="think('${p.id}', this)">I'm thinking of you!</button>
                <div class="action-btns">
                    <button class="secondary-btn" onclick="showStats('${p.id}', '${p.partnerUsername}')">Stats</button>
                    <button class="danger-btn" onclick="deleteConnection('${p.id}')">Disconnect</button>
                </div>
            `;
            list.appendChild(card);
        });
    }
}

async function loadRequests() {
    const res = await fetch('/api/connections/requests', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const requests = await res.json();
    const section = document.getElementById('pending-requests');
    const list = document.getElementById('requests-list');
    list.innerHTML = '';

    if (requests.length > 0) {
        section.classList.remove('hidden');
        requests.forEach(r => {
            const item = document.createElement('div');
            item.className = 'request-item';
            item.innerHTML = `
                <span>${r.partnerUsername} wants to connect</span>
                <div class="button-group">
                    <button onclick="acceptRequest('${r.id}')">Accept</button>
                    <button class="secondary-btn" onclick="rejectRequest('${r.id}')">Reject</button>
                </div>
            `;
            list.appendChild(item);
        });
    } else {
        section.classList.add('hidden');
    }
}

async function searchUser() {
    const user = document.getElementById('search-username').value;
    const res = await fetch(`/api/users/search?username=${user}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const resultDiv = document.getElementById('search-result');
    if (res.ok) {
        const u = await res.json();
        resultDiv.innerHTML = `
            <div class="request-item">
                <span>${u.username}</span>
                <button onclick="sendRequest('${u.username}')">Send Connection Request</button>
            </div>
        `;
    } else {
        resultDiv.innerHTML = '<p>User not found</p>';
    }
}

async function sendRequest(username) {
    await fetch('/api/connections/request', {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ username })
    });
    showToast(`Request sent to ${username}!`, 'success');
    showDashboard();
}

async function acceptRequest(id) {
    await fetch(`/api/connections/${id}/accept`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    loadDashboard();
}

async function rejectRequest(id) {
    await fetch(`/api/connections/${id}/reject`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    loadRequests();
}

async function deleteConnection(id) {
    if (confirm('Are you sure you want to disconnect? This action cannot be undone.')) {
        await fetch(`/api/connections/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        showToast('Connection removed.', 'info');
        loadPartners();
    }
}

async function think(id, btn) {
    btn.classList.add('pulse');
    setTimeout(() => btn.classList.remove('pulse'), 300);
    
    const mood = selectedMoods[id] || 'none';
    
    await fetch(`/api/connections/${id}/think`, {
        method: 'POST',
        headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ mood })
    });
    // Update local counter immediately for better UX
    const val = document.getElementById(`sent-${id}`);
    val.innerText = parseInt(val.innerText) + 1;
    const moodEmoji = document.querySelector(`#mood-selector-${id} .mood-btn.selected`).textContent.split(' ')[0];
    showToast(`Sent a thought ${moodEmoji}!`, 'success');
}

function selectMood(mood, connectionId) {
    selectedMoods[connectionId] = mood;
    
    // Update UI
    const buttons = document.querySelectorAll(`#mood-selector-${connectionId} .mood-btn`);
    buttons.forEach(btn => {
        if (btn.dataset.mood === mood) {
            btn.classList.add('selected');
        } else {
            btn.classList.remove('selected');
        }
    });
}

function getMoodEmoji(mood) {
    const moodEmojis = {
        'happy': '😊',
        'love': '❤️',
        'sad': '😢',
        'angry': '😠',
        'excited': '🤗',
        'worried': '😟',
        'grateful': '🙏',
        'none': '💭'
    };
    return moodEmojis[mood] || '💭';
}

// WebSocket
function connectWebSocket() {
    if (stompClient) return;
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/updates/${currentUsername}`, () => {
            loadPartners();
            loadRequests();
        });
    });
}

// Statistics
async function loadStats() {
    let loader = showToast('Loading statistics...', 'info');
    try {
        const days = document.getElementById('stats-range').value;
        const bucket = document.getElementById('stats-bucket').value;
        const direction = document.getElementById('stats-direction').value;

        const toDate = new Date();
        const fromDate = new Date(toDate.getTime() - (days * 24 * 60 * 60 * 1000));
        
        const toISO = toDate.toISOString();
        const fromISO = fromDate.toISOString();

        const res = await fetch(`/api/metrics/moods?connectionId=${currentStatsConnectionId}&from=${fromISO}&to=${toISO}&bucketMinutes=${bucket}&direction=${direction}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (loader) loader.remove();

        // Prepare labels from time buckets
        const labels = Object.keys(data.timeBuckets).map(k => {
            const d = new Date(k);
            return days === '1' ? d.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : d.toLocaleDateString();
        });

        // Define mood colors and emojis
        const moodColors = {
            'happy': '#FFD700',
            'sad': '#4169E1',
            'angry': '#FF4500',
            'love': '#FF69B4',
            'excited': '#FFA500',
            'worried': '#9370DB',
            'grateful': '#32CD32',
            'none': '#808080'
        };

        const moodEmojis = {
            'happy': '😊',
            'sad': '😢',
            'angry': '😠',
            'love': '❤️',
            'excited': '🤗',
            'worried': '😟',
            'grateful': '🙏',
            'none': '💭'
        };

        // Prepare datasets for each mood
        const datasets = [];
        const moods = ['happy', 'sad', 'angry', 'love', 'excited', 'worried', 'grateful', 'none'];
        
        moods.forEach(mood => {
            const values = Object.values(data.timeBuckets).map(bucket => {
                // Access the mood count using the lowercase key
                return bucket[mood] || 0;
            });
            datasets.push({
                label: `${moodEmojis[mood]} ${mood.charAt(0).toUpperCase() + mood.slice(1)}`,
                data: values,
                backgroundColor: moodColors[mood],
                borderRadius: 2
            });
        });
        
        if (chart) chart.destroy();
        const ctx = document.getElementById('statsChart').getContext('2d');
        chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { stacked: true },
                    y: { 
                        stacked: true,
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' + context.parsed.y;
                            }
                        }
                    }
                }
            }
        });

        // Display mood distribution summary
        displayMoodSummary(data.totalMoodDistribution, moodEmojis);
    } catch (e) {
        console.error(e);
        if (loader) loader.remove();
        showToast('Failed to load statistics', 'error');
    }
}

function displayMoodSummary(totalMoodDistribution, moodEmojis) {
    // Remove existing summary if present
    const existingSummary = document.getElementById('mood-summary');
    if (existingSummary) {
        existingSummary.remove();
    }

    // Create mood summary container
    const summaryContainer = document.createElement('div');
    summaryContainer.id = 'mood-summary';

    const title = document.createElement('h3');
    title.textContent = 'Mood Distribution Summary';
    summaryContainer.appendChild(title);

    const moodGrid = document.createElement('div');
    moodGrid.className = 'mood-grid';

    // Sort moods by count (descending)
    const sortedMoods = Object.entries(totalMoodDistribution)
        .filter(([mood, count]) => count > 0)
        .sort((a, b) => b[1] - a[1]);

    if (sortedMoods.length === 0) {
        moodGrid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #666;">No mood data available</p>';
    } else {
        sortedMoods.forEach(([mood, count]) => {
            const moodItem = document.createElement('div');
            moodItem.className = 'mood-item';
            
            const emoji = document.createElement('div');
            emoji.className = 'mood-emoji';
            emoji.textContent = moodEmojis[mood.toLowerCase()] || '❓';
            
            const name = document.createElement('div');
            name.className = 'mood-name';
            name.textContent = mood.charAt(0).toUpperCase() + mood.slice(1).toLowerCase();
            
            const countDiv = document.createElement('div');
            countDiv.className = 'mood-count';
            countDiv.textContent = count;
            
            moodItem.appendChild(emoji);
            moodItem.appendChild(name);
            moodItem.appendChild(countDiv);
            moodGrid.appendChild(moodItem);
        });
    }

    summaryContainer.appendChild(moodGrid);

    // Insert summary after the chart container
    const chartContainer = document.querySelector('.chart-container');
    chartContainer.parentNode.insertBefore(summaryContainer, chartContainer.nextSibling);
}
