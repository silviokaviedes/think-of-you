<template>
  <div id="app">
    <header>
      <h1>Thinking of You</h1>
      <div
        id="notification-icon-mobile"
        class="notification-icon-mobile"
        :class="{ hidden: !isAuthenticated || !hasPendingRequests }"
        title="You have pending connection requests"
        @click="showDashboard"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7zm2.85 11.1l-.85.6V16h-4v-2.3l-.85-.6A4.997 4.997 0 0 1 7 9c0-2.76 2.24-5 5-5s5 2.24 5 5c0 1.63-.8 3.16-2.15 4.1z"
          />
        </svg>
      </div>
      <button
        class="burger-menu"
        :class="{ active: isMenuOpen, 'has-pending': hasPendingRequests }"
        v-if="isAuthenticated"
        aria-label="Toggle menu"
        @click="toggleMenu"
      >
        <span></span>
        <span></span>
        <span></span>
        <span v-if="hasPendingRequests" class="indicator-icon burger-indicator">!</span>
      </button>
      <nav v-if="isAuthenticated" class="header-nav" :class="{ active: isMenuOpen }" id="header-nav">
        <button
          class="nav-btn"
          :class="{ 'has-pending': hasPendingRequests }"
          @click="showDashboard"
        >
          Dashboard
          <span v-if="hasPendingRequests" class="indicator-icon">!</span>
        </button>
        <button class="nav-btn" @click="showSearch">Search Partner</button>
        <div id="user-info" :class="{ hidden: !isAuthenticated }">
          <span id="current-username">{{ currentUsername }}</span>
          <button class="secondary-btn" @click="logout">Logout</button>
        </div>
      </nav>
    </header>

    <main>
      <section id="auth-section" v-if="currentView === 'auth'">
        <div class="card" style="max-width: 400px; margin: 0 auto;">
          <h2>Welcome Back</h2>
          <p style="color: var(--text-light); margin-bottom: 20px;">Connect with your loved ones.</p>
          <input v-model.trim="authUsername" type="text" placeholder="Username" />
          <input v-model.trim="authPassword" type="password" placeholder="Password" />
          <div class="button-group" style="margin-top: 20px;">
            <button style="flex: 2" :disabled="isAuthBusy" @click="login">Login</button>
            <button class="secondary-btn" style="flex: 1" :disabled="isAuthBusy" @click="register">Register</button>
          </div>
        </div>
      </section>

      <section id="dashboard-section" v-if="currentView === 'dashboard'">
        <div class="card">
          <h2>My Partners</h2>
        </div>

        <div id="partners-list" class="partners-grid">
          <div v-for="partner in partners" :key="partner.id" class="partner-card">
            <h3>{{ partner.partnerUsername }}</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">Received</span>
                <span class="stat-value">{{ partner.receivedClicks }}</span>
                <span v-if="partner.lastReceivedMood && partner.lastReceivedMood !== 'none'" class="last-mood">
                  {{ getMoodEmoji(partner.lastReceivedMood) }}
                </span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Sent</span>
                <span class="stat-value">{{ partner.sentClicks }}</span>
              </div>
            </div>

            <div class="mood-selector">
              <label class="mood-label">How are you feeling?</label>
              <div class="mood-options">
                <button
                  v-for="option in moodOptions"
                  :key="option.value"
                  class="mood-btn"
                  :class="{ selected: selectedMoods[partner.id] === option.value }"
                  @click="selectMood(option.value, partner.id)"
                >
                  {{ option.label }}
                </button>
              </div>
            </div>

            <button class="think-btn" @click="think(partner.id)">I'm thinking of you!</button>
            <div class="action-btns">
              <button class="secondary-btn" @click="showStats(partner.id, partner.partnerUsername)">Stats</button>
              <button class="danger-btn" @click="deleteConnection(partner.id)">Disconnect</button>
            </div>
          </div>
        </div>

        <div id="no-partners" :class="{ hidden: partners.length !== 0 }">
          <p>No partners yet.</p>
          <button @click="showSearch">Find a partner now!</button>
        </div>

        <div id="pending-requests" :class="{ hidden: pendingRequests.length === 0 }">
          <h3>Pending Requests</h3>
          <div id="requests-list">
            <div v-for="request in pendingRequests" :key="request.id" class="request-item">
              <span>{{ request.partnerUsername }} wants to connect</span>
              <div class="button-group">
                <button @click="acceptRequest(request.id)">Accept</button>
                <button class="secondary-btn" @click="rejectRequest(request.id)">Reject</button>
              </div>
            </div>
          </div>
        </div>

        <div id="sent-requests" :class="{ hidden: sentRequests.length === 0 }">
          <h3>Sent Requests</h3>
          <div id="sent-requests-list">
            <div v-for="request in sentRequests" :key="request.id" class="request-item">
              <span>Request sent to {{ request.partnerUsername }}</span>
              <div class="button-group">
                <button class="danger-btn" @click="cancelRequest(request.id)">Cancel</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="search-section" v-if="currentView === 'search'">
        <div class="card">
          <div class="card-header">
            <h2>Search Partner</h2>
            <button class="close-btn" @click="showDashboard">Back</button>
          </div>
          <div class="search-box">
            <input v-model.trim="searchUsername" type="text" placeholder="Exact username" />
            <button @click="searchUser">Search</button>
          </div>
          <div id="search-result">
            <div v-if="searchResult" class="request-item">
              <span>{{ searchResult }}</span>
              <button @click="sendRequest(searchResult)">Send Connection Request</button>
            </div>
            <p v-else-if="searchNotFound">User not found</p>
          </div>
        </div>
      </section>

      <section id="stats-section" v-if="currentView === 'stats'">
        <div class="card full-width">
          <div class="card-header">
            <h2 id="stats-title">{{ statsTitle }}</h2>
            <button class="close-btn" @click="showDashboard">Close</button>
          </div>
          <div class="stats-controls">
            <select v-model.number="statsRange" @change="loadStats">
              <option :value="1">Last 24 Hours</option>
              <option :value="7">Last 7 Days</option>
              <option :value="30">Last 30 Days</option>
            </select>
            <select v-model.number="statsBucket" @change="loadStats">
              <option :value="60">1 Hour</option>
              <option :value="360">6 Hours</option>
              <option :value="1440">1 Day</option>
            </select>
            <select v-model="statsDirection" @change="loadStats">
              <option value="received">Received</option>
              <option value="sent">Sent</option>
            </select>
          </div>
          <div class="chart-container">
            <canvas ref="chartCanvas"></canvas>
          </div>
          <div id="mood-summary" v-if="moodSummary.length">
            <h3>Mood Distribution Summary</h3>
            <div class="mood-grid">
              <div v-for="mood in moodSummary" :key="mood.name" class="mood-item">
                <div class="mood-emoji">{{ mood.emoji }}</div>
                <div class="mood-name">{{ mood.label }}</div>
                <div class="mood-count">{{ mood.count }}</div>
              </div>
            </div>
          </div>
          <div id="mood-summary" v-else>
            <h3>Mood Distribution Summary</h3>
            <div class="mood-grid">
              <p style="grid-column: 1 / -1; text-align: center; color: #666;">No mood data available</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>

  <div id="toast-container">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="toast.type"
    >
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Chart from 'chart.js/auto';

const token = ref<string>(localStorage.getItem('token') ?? '');
const currentUsername = ref<string>(localStorage.getItem('username') ?? '');
const isAuthenticated = computed(() => Boolean(token.value));

const currentView = ref<'auth' | 'dashboard' | 'search' | 'stats'>(
  isAuthenticated.value ? 'dashboard' : 'auth'
);
const isMenuOpen = ref(false);

const authUsername = ref('');
const authPassword = ref('');
const isAuthBusy = ref(false);

const partners = ref<ConnectionDTO[]>([]);
const pendingRequests = ref<ConnectionDTO[]>([]);
const sentRequests = ref<ConnectionDTO[]>([]);
const selectedMoods = ref<Record<string, Mood>>({});

const searchUsername = ref('');
const searchResult = ref<string | null>(null);
const searchNotFound = ref(false);

const currentStatsConnectionId = ref<string>('');
const statsTitle = ref('Statistics');
const statsRange = ref(1);
const statsBucket = ref(60);
const statsDirection = ref<'received' | 'sent'>('received');
const statsData = ref<MoodMetricsDTO | null>(null);

const chartCanvas = ref<HTMLCanvasElement | null>(null);
let chart: Chart | null = null;

const toasts = ref<ToastItem[]>([]);
let toastId = 0;

let stompClient: Client | null = null;

const hasPendingRequests = computed(() => pendingRequests.value.length > 0);

const moodOptions: MoodOption[] = [
  { value: 'happy', label: `${getMoodEmoji('happy')} Happy` },
  { value: 'love', label: `${getMoodEmoji('love')} Love` },
  { value: 'sad', label: `${getMoodEmoji('sad')} Sad` },
  { value: 'angry', label: `${getMoodEmoji('angry')} Angry` },
  { value: 'excited', label: `${getMoodEmoji('excited')} Excited` },
  { value: 'worried', label: `${getMoodEmoji('worried')} Worried` },
  { value: 'grateful', label: `${getMoodEmoji('grateful')} Grateful` },
  { value: 'none', label: `${getMoodEmoji('none')} Neutral` }
];

const moodSummary = computed(() => {
  const distribution = statsData.value?.totalMoodDistribution ?? {};
  const entries = Object.entries(distribution)
    .filter(([, count]) => count > 0)
    .sort((a, b) => b[1] - a[1]);

  return entries.map(([name, count]) => {
    const mood = name.toLowerCase() as Mood;
    return {
      name,
      label: mood.charAt(0).toUpperCase() + mood.slice(1),
      emoji: getMoodEmoji(mood),
      count
    };
  });
});

onMounted(() => {
  if (isAuthenticated.value) {
    showDashboard();
    connectWebSocket();
  }
});

function toggleMenu() {
  isMenuOpen.value = !isMenuOpen.value;
}

function showDashboard() {
  currentView.value = 'dashboard';
  isMenuOpen.value = false;
  loadPartners();
  loadRequests();
}

function showSearch() {
  currentView.value = 'search';
  isMenuOpen.value = false;
  searchResult.value = null;
  searchNotFound.value = false;
}

function showStats(connectionId: string, partnerName: string) {
  currentStatsConnectionId.value = connectionId;
  statsTitle.value = `Statistics for ${partnerName}`;
  currentView.value = 'stats';
  isMenuOpen.value = false;
  loadStats();
}

function showToast(message: string, type: ToastType = 'info', sticky = false) {
  const id = ++toastId;
  toasts.value.push({ id, message, type, sticky });

  if (!sticky) {
    window.setTimeout(() => {
      toasts.value = toasts.value.filter((toast) => toast.id !== id);
    }, 3000);
  }

  return id;
}

function clearToast(id: number) {
  toasts.value = toasts.value.filter((toast) => toast.id !== id);
}

async function login() {
  if (!authUsername.value || !authPassword.value) {
    showToast('Please enter credentials', 'error');
    return;
  }

  isAuthBusy.value = true;
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: authUsername.value, password: authPassword.value })
    });

    if (res.ok) {
      const data = (await res.json()) as AuthResponse;
      token.value = data.token;
      currentUsername.value = data.username;
      localStorage.setItem('token', token.value);
      localStorage.setItem('username', currentUsername.value);
      showDashboard();
      connectWebSocket();
      showToast(`Welcome back, ${currentUsername.value}!`, 'success');
    } else {
      showToast('Login failed. Please check your credentials.', 'error');
    }
  } catch (error) {
    console.error(error);
    showToast('Connection error', 'error');
  } finally {
    isAuthBusy.value = false;
  }
}

async function register() {
  if (!authUsername.value || !authPassword.value) {
    showToast('Please enter credentials', 'error');
    return;
  }

  isAuthBusy.value = true;
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: authUsername.value, password: authPassword.value })
    });

    if (res.ok) {
      showToast('Registered successfully! You can now login.', 'success');
    } else {
      showToast('Registration failed. Username might be taken.', 'error');
    }
  } catch (error) {
    console.error(error);
    showToast('Connection error', 'error');
  } finally {
    isAuthBusy.value = false;
  }
}

function logout() {
  localStorage.clear();
  token.value = '';
  currentUsername.value = '';
  disconnectWebSocket();
  currentView.value = 'auth';
  partners.value = [];
  pendingRequests.value = [];
  sentRequests.value = [];
  selectedMoods.value = {};
}

async function loadPartners() {
  const res = await apiFetch('/api/connections');
  if (!res) return;
  if (!res.ok) return;
  const data = (await res.json()) as ConnectionDTO[];
  partners.value = data;

  data.forEach((partner) => {
    if (!selectedMoods.value[partner.id]) {
      selectedMoods.value[partner.id] = 'none';
    }
  });
}

async function loadRequests() {
  const res = await apiFetch('/api/connections/requests');
  if (!res) return;
  if (res.ok) {
    pendingRequests.value = (await res.json()) as ConnectionDTO[];
  }

  const sentRes = await apiFetch('/api/connections/sent');
  if (!sentRes) return;
  if (sentRes.ok) {
    sentRequests.value = (await sentRes.json()) as ConnectionDTO[];
  }
}

async function searchUser() {
  if (!searchUsername.value) return;
  const res = await apiFetch(`/api/users/search?username=${encodeURIComponent(searchUsername.value)}`);
  if (!res) return;
  if (res.ok) {
    const data = (await res.json()) as { username: string };
    searchResult.value = data.username;
    searchNotFound.value = false;
  } else {
    searchResult.value = null;
    searchNotFound.value = true;
  }
}

async function sendRequest(username: string) {
  const res = await apiFetch('/api/connections/request', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username })
  });
  if (!res) return;
  showToast(`Request sent to ${username}!`, 'success');
  showDashboard();
}

async function acceptRequest(id: string) {
  const res = await apiFetch(`/api/connections/${id}/accept`, { method: 'POST' });
  if (!res) return;
  showDashboard();
}

async function rejectRequest(id: string) {
  const res = await apiFetch(`/api/connections/${id}/reject`, { method: 'POST' });
  if (!res) return;
  loadRequests();
}

async function cancelRequest(id: string) {
  const res = await apiFetch(`/api/connections/${id}/cancel`, { method: 'POST' });
  if (!res) return;
  showToast('Request cancelled.', 'info');
  loadRequests();
}

async function deleteConnection(id: string) {
  if (!window.confirm('Are you sure you want to disconnect? This action cannot be undone.')) return;
  const res = await apiFetch(`/api/connections/${id}`, { method: 'DELETE' });
  if (!res) return;
  showToast('Connection removed.', 'info');
  loadPartners();
}

async function think(id: string) {
  const mood = selectedMoods.value[id] ?? 'none';
  const res = await apiFetch(`/api/connections/${id}/think`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ mood })
  });
  if (!res) return;

  partners.value = partners.value.map((partner) => {
    if (partner.id === id) {
      return { ...partner, sentClicks: partner.sentClicks + 1 };
    }
    return partner;
  });

  showToast(`Sent a thought ${getMoodEmoji(mood)}!`, 'success');
}

function selectMood(mood: Mood, connectionId: string) {
  selectedMoods.value[connectionId] = mood;
}

function connectWebSocket() {
  if (stompClient || !currentUsername.value) return;

  const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    reconnectDelay: 5000
  });

  client.onConnect = () => {
    client.subscribe(`/topic/updates/${currentUsername.value}`, (message) => {
      const data = message.body;
      if (data.startsWith('{"type":"thought"')) {
        try {
          const thought = JSON.parse(data) as ThoughtMessage;
          showToast(`${thought.sender} is thinking of you ${thought.emoji}!`, 'success');
        } catch (error) {
          console.error(error);
        }
        loadPartners();
      } else {
        loadPartners();
        loadRequests();
      }
    });
  };

  client.onStompError = (frame) => {
    console.error('STOMP error', frame.headers['message']);
  };

  client.onWebSocketError = (event) => {
    console.error('WebSocket error', event);
  };

  client.activate();
  stompClient = client;
}

function disconnectWebSocket() {
  if (!stompClient) return;
  stompClient.deactivate();
  stompClient = null;
}

async function loadStats() {
  if (!currentStatsConnectionId.value) return;
  const loaderId = showToast('Loading statistics...', 'info', true);

  try {
    const toDate = new Date();
    const fromDate = new Date(toDate.getTime() - statsRange.value * 24 * 60 * 60 * 1000);

    const toISO = toDate.toISOString();
    const fromISO = fromDate.toISOString();

    const res = await apiFetch(
      `/api/metrics/moods?connectionId=${currentStatsConnectionId.value}` +
        `&from=${fromISO}` +
        `&to=${toISO}` +
        `&bucketMinutes=${statsBucket.value}` +
        `&direction=${statsDirection.value}`
    );

    if (!res || !res.ok) {
      showToast('Failed to load statistics', 'error');
      return;
    }

    statsData.value = (await res.json()) as MoodMetricsDTO;
    await nextTick();
    renderChart();
  } catch (error) {
    console.error(error);
    showToast('Failed to load statistics', 'error');
  } finally {
    clearToast(loaderId);
  }
}

function renderChart() {
  if (!chartCanvas.value || !statsData.value) return;

  const labels = Object.keys(statsData.value.timeBuckets).map((key) => {
    const date = new Date(key);
    if (statsRange.value === 1) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString();
  });

  const moodColors: Record<Mood, string> = {
    happy: '#FFD700',
    sad: '#4169E1',
    angry: '#FF4500',
    love: '#FF69B4',
    excited: '#FFA500',
    worried: '#9370DB',
    grateful: '#32CD32',
    none: '#808080'
  };

  const moods: Mood[] = ['happy', 'sad', 'angry', 'love', 'excited', 'worried', 'grateful', 'none'];

  const datasets = moods.map((mood) => {
    const values = Object.values(statsData.value!.timeBuckets).map((bucket) => bucket[mood] || 0);
    return {
      label: `${getMoodEmoji(mood)} ${mood.charAt(0).toUpperCase() + mood.slice(1)}`,
      data: values,
      backgroundColor: moodColors[mood],
      borderRadius: 2
    };
  });

  if (chart) {
    chart.destroy();
  }

  chart = new Chart(chartCanvas.value, {
    type: 'bar',
    data: { labels, datasets },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: { stacked: true },
        y: { stacked: true, beginAtZero: true, ticks: { stepSize: 1 } }
      },
      plugins: {
        tooltip: {
          callbacks: {
            label: (context) => `${context.dataset.label}: ${context.parsed.y}`
          }
        }
      }
    }
  });
}

function getMoodEmoji(mood: Mood) {
  switch (mood) {
    case 'happy':
      return '\uD83D\uDE0A';
    case 'sad':
      return '\uD83D\uDE22';
    case 'angry':
      return '\uD83D\uDE20';
    case 'love':
      return '\u2764\uFE0F';
    case 'excited':
      return '\uD83E\uDD17';
    case 'worried':
      return '\uD83D\uDE1F';
    case 'grateful':
      return '\uD83D\uDE4F';
    case 'none':
      return '\uD83D\uDCAD';
    default:
      return '\uD83D\uDCAD';
  }
}

async function apiFetch(input: RequestInfo, init: RequestInit = {}) {
  if (!token.value) {
    return null;
  }

  const headers = new Headers(init.headers ?? {});
  headers.set('Authorization', `Bearer ${token.value}`);

  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  try {
    const res = await fetch(input, { ...init, headers });
    if (res.status === 401) {
      logout();
      showToast('Session expired. Please log in again.', 'error');
      return null;
    }
    return res;
  } catch (error) {
    console.error(error);
    showToast('Connection error', 'error');
    return null;
  }
}

interface AuthResponse {
  token: string;
  username: string;
}

interface ConnectionDTO {
  id: string;
  partnerUsername: string;
  receivedClicks: number;
  sentClicks: number;
  status: string;
  lastReceivedMood?: Mood | null;
}

type Mood = 'happy' | 'sad' | 'angry' | 'love' | 'excited' | 'worried' | 'grateful' | 'none';

interface MoodMetricsDTO {
  timeBuckets: Record<string, Record<Mood, number>>;
  totalMoodDistribution: Record<string, number>;
}

interface ThoughtMessage {
  type: 'thought';
  sender: string;
  mood: Mood;
  emoji: string;
}

interface ToastItem {
  id: number;
  message: string;
  type: ToastType;
  sticky: boolean;
}

type ToastType = 'info' | 'success' | 'error';

interface MoodOption {
  value: Mood;
  label: string;
}
</script>
