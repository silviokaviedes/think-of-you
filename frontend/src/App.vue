<template>
  <div id="app">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">💭</span>
        <h1>Thinking of You</h1>
      </div>
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
        <div class="nav-actions">
          <button
            class="nav-btn"
            :class="{ 'has-pending': hasPendingRequests, active: currentView === 'dashboard' }"
            @click="showDashboard"
          >
            Dashboard
            <span v-if="hasPendingRequests" class="indicator-icon">!</span>
          </button>
          <button class="nav-btn" :class="{ active: currentView === 'search' }" @click="showSearch">Search</button>
          <button class="nav-btn" :class="{ active: currentView === 'stats' }" @click="openStatsFromNav">Stats</button>
          <button class="nav-btn" :class="{ active: currentView === 'profile' }" @click="showProfile">Profile</button>
          <button class="nav-btn" :class="{ active: currentView === 'events' }" @click="showEventLog">Event Log</button>
          <button class="nav-btn" :class="{ active: currentView === 'news' }" @click="showNews">News</button>
        </div>
        <div id="user-info" :class="{ hidden: !isAuthenticated }">
          <span id="current-username">{{ currentUsername }}</span>
          <button class="secondary-btn" @click="logout">Logout</button>
        </div>
      </nav>
      <button
        v-else
        class="nav-btn"
        :class="{ active: currentView === 'news' }"
        @click="showNews"
      >
        News
      </button>
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
          <h2>My People</h2>
        </div>

        <div id="partners-list" class="partners-grid">
          <div v-for="partner in partners" :key="partner.id" class="partner-card">
            <h3>{{ partner.partnerUsername }}</h3>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">Received</span>
                <template v-if="dashboardDisplayMode === 'counts'">
                  <span class="stat-value">{{ partner.receivedClicks }}</span>
                  <span v-if="partner.lastReceivedMood && partner.lastReceivedMood !== 'none'" class="last-mood">
                    {{ getMoodEmoji(partner.lastReceivedMood) }}
                  </span>
                </template>
                <span v-else class="stat-detail">
                  <template v-if="partner.lastReceivedAt">
                    <span class="stat-value">{{ getMoodEmoji(partner.lastReceivedMood ?? 'none') }}</span>
                    <span class="stat-time">{{ formatEventTime(partner.lastReceivedAt) }}</span>
                  </template>
                  <template v-else>No events yet</template>
                </span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Sent</span>
                <template v-if="dashboardDisplayMode === 'counts'">
                  <span class="stat-value">{{ partner.sentClicks }}</span>
                  <span v-if="partner.lastSentMood && partner.lastSentMood !== 'none'" class="last-mood">
                    {{ getMoodEmoji(partner.lastSentMood) }}
                  </span>
                </template>
                <span v-else class="stat-detail">
                  <template v-if="partner.lastSentAt">
                    <span class="stat-value">{{ getMoodEmoji(partner.lastSentMood ?? 'none') }}</span>
                    <span class="stat-time">{{ formatEventTime(partner.lastSentAt) }}</span>
                  </template>
                  <template v-else>No events yet</template>
                </span>
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
                  <span class="mood-emoji">{{ option.emoji }}</span>
                  <span class="mood-text">{{ option.label }}</span>
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
          <button @click="showSearch">Find someone</button>
        </div>

        <div id="pending-requests" :class="{ hidden: pendingRequests.length === 0 }">
          <h3>Waiting to connect</h3>
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
            <h2>Find someone</h2>
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
            <div class="stats-header">
              <h2 id="stats-title">{{ statsTitle }}</h2>
              <select
                v-if="partners.length"
                class="stats-connection-select"
                v-model="currentStatsConnectionId"
                @change="onStatsConnectionChange"
              >
                <option v-for="partner in partners" :key="partner.id" :value="partner.id">
                  {{ partner.partnerUsername }}
                </option>
              </select>
            </div>
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

      <section id="profile-section" v-if="currentView === 'profile'">
        <div class="card" style="max-width: 520px; margin: 0 auto;">
          <div class="card-header">
            <h2>Profile</h2>
            <button class="close-btn" @click="showDashboard">Back</button>
          </div>
          <p style="color: var(--text-light); margin-bottom: 16px;">
            Change your password for <strong>{{ currentUsername }}</strong>
          </p>
          <input v-model="profileCurrentPassword" type="password" placeholder="Current password" />
          <input v-model="profileNewPassword" type="password" placeholder="New password" />
          <input v-model="profileConfirmPassword" type="password" placeholder="Confirm new password" />
          <div class="button-group" style="margin-top: 12px;">
            <button :disabled="isPasswordBusy" @click="changePassword">Update password</button>
          </div>
          <div style="margin-top: 20px;">
            <h3 style="margin-bottom: 8px;">Dashboard event display</h3>
            <p style="color: var(--text-light); margin-bottom: 12px;">
              Choose whether dashboard cards show total counts or only the latest event date/time + emoji.
            </p>
            <select v-model="dashboardDisplayMode" style="width: 100%; margin-bottom: 12px;">
              <option value="counts">Show total sent/received counts</option>
              <option value="last_event">Show only last event timestamp + emoji</option>
            </select>
            <div class="button-group" style="margin-top: 12px;">
              <button :disabled="isDashboardPreferenceBusy" @click="saveDashboardPreference">
                Save dashboard view
              </button>
            </div>
          </div>
          <div style="margin-top: 20px;">
            <h3 style="margin-bottom: 8px;">Favorite emojis for dashboard</h3>
            <p style="color: var(--text-light); margin-bottom: 12px;">
              Choose up to {{ maxFavoriteMoods }} emojis. These are shown when sending a thought.
            </p>
            <div class="mood-options">
              <button
                v-for="option in moodCatalog"
                :key="`pref-${option.value}`"
                class="mood-btn"
                :class="{ selected: favoriteMoods.includes(option.value) }"
                @click="toggleFavoriteMood(option.value)"
              >
                <span class="mood-emoji">{{ option.emoji }}</span>
                <span class="mood-text">{{ option.label }}</span>
              </button>
            </div>
            <div class="button-group" style="margin-top: 12px;">
              <button :disabled="isMoodPreferencesBusy" @click="saveMoodPreferences">Save emoji preferences</button>
            </div>
          </div>
        </div>
      </section>

      <section id="event-log-section" v-if="currentView === 'events'">
        <div class="card full-width">
          <div class="card-header">
            <h2>Event Log</h2>
            <button class="close-btn" @click="showDashboard">Back</button>
          </div>
          <p style="color: var(--text-light); margin-bottom: 12px;">Most recent events appear first.</p>
          <div class="stats-controls">
            <select v-model.number="eventLogLimit" @change="loadEventLog">
              <option :value="10">10 events</option>
              <option :value="25">25 events</option>
              <option :value="50">50 events</option>
              <option :value="100">100 events</option>
            </select>
            <select v-model="eventLogDirection" @change="loadEventLog">
              <option value="all">Sent + Received</option>
              <option value="sent">Sent only</option>
              <option value="received">Received only</option>
            </select>
          </div>
          <div v-if="eventLogItems.length === 0" style="text-align: center; color: #666; padding: 12px 0;">
            No events yet.
          </div>
          <div v-else style="display: flex; flex-direction: column; gap: 10px;">
            <div
              v-for="item in eventLogItems"
              :key="`${item.connectionId}-${item.occurredAt}-${item.direction}`"
              style="
                background: #fff;
                border: 1px solid rgba(0, 0, 0, 0.05);
                border-left: 4px solid var(--primary);
                border-radius: 10px;
                padding: 12px 14px;
                text-align: left;
              "
            >
              <div style="font-size: 12px; font-weight: 700; color: var(--text-light);">
                {{ formatEventTime(item.occurredAt) }}
              </div>
              <div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">
                <span
                  :title="getMoodLabel(item.mood)"
                  style="font-size: 2em; line-height: 1; cursor: help; display: inline-flex; min-width: 1.25em; justify-content: center;"
                >
                  {{ getMoodEmoji(item.mood) }}
                </span>
                <div style="font-size: 14px;">
                  <strong>{{ item.direction === 'sent' ? 'Sent' : 'Received' }}</strong>
                  {{ item.direction === 'sent' ? `to ${item.partnerUsername}` : `from ${item.partnerUsername}` }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="news-section" v-if="currentView === 'news'">
        <div class="card full-width">
          <div class="card-header">
            <h2>Latest News</h2>
            <button class="close-btn" @click="closeNews">{{ isAuthenticated ? 'Back' : 'Back to Login' }}</button>
          </div>
          <p style="color: var(--text-light); margin-bottom: 16px;">
            Recent updates and newest features in Thinking of You.
          </p>
          <div class="mood-grid">
            <div v-for="item in newsItems" :key="item.title" class="mood-item" style="text-align: left;">
              <div class="mood-name" style="font-size: 12px; font-weight: 700;">{{ item.date }}</div>
              <div style="font-size: 16px; font-weight: 700; margin: 4px 0;">{{ item.title }}</div>
              <div style="font-size: 14px; color: var(--text-light);">{{ item.description }}</div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>

  <div id="toast-container" :class="{ 'with-bottom-nav': isAuthenticated }">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      class="toast"
      :class="toast.type"
    >
      {{ toast.message }}
    </div>
  </div>

  <nav v-if="isAuthenticated" class="bottom-nav">
    <button class="tab-btn" :class="{ active: currentView === 'dashboard' }" @click="showDashboard">
      Home
      <span v-if="hasPendingRequests" class="tab-dot"></span>
    </button>
    <button class="tab-btn" :class="{ active: currentView === 'search' }" @click="showSearch">
      Search
    </button>
    <button class="tab-btn" :class="{ active: currentView === 'stats' }" @click="openStatsFromNav">
      Stats
    </button>
    <button class="tab-btn" :class="{ active: currentView === 'events' }" @click="showEventLog">
      Event Log
    </button>
  </nav>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Chart from 'chart.js/auto';
import { Capacitor } from '@capacitor/core';
import { PushNotifications } from '@capacitor/push-notifications';

const token = ref<string>(localStorage.getItem('token') ?? '');
const refreshToken = ref<string>(localStorage.getItem('refreshToken') ?? '');
const currentUsername = ref<string>(localStorage.getItem('username') ?? '');
const isAuthenticated = computed(() => Boolean(token.value));

const currentView = ref<'auth' | 'dashboard' | 'search' | 'stats' | 'profile' | 'events' | 'news'>(
  isAuthenticated.value ? 'dashboard' : 'auth'
);
const isMenuOpen = ref(false);

const authUsername = ref('');
const authPassword = ref('');
const isAuthBusy = ref(false);

const partners = ref<ConnectionDTO[]>([]);
const pendingRequests = ref<ConnectionDTO[]>([]);
const sentRequests = ref<ConnectionDTO[]>([]);
const selectedMoods = ref<Record<string, string>>({});

const searchUsername = ref('');
const searchResult = ref<string | null>(null);
const searchNotFound = ref(false);

const profileCurrentPassword = ref('');
const profileNewPassword = ref('');
const profileConfirmPassword = ref('');
const isPasswordBusy = ref(false);
const isMoodPreferencesBusy = ref(false);
const isDashboardPreferenceBusy = ref(false);
const dashboardDisplayMode = ref<DashboardDisplayMode>('counts');

const currentStatsConnectionId = ref<string>('');
const statsTitle = ref('Statistics');
const statsRange = ref(1);
const statsBucket = ref(60);
const statsDirection = ref<'received' | 'sent'>('received');
const statsData = ref<MoodMetricsDTO | null>(null);
const eventLogItems = ref<EventLogItemDTO[]>([]);
const eventLogLimit = ref(25);
const eventLogDirection = ref<'all' | 'sent' | 'received'>('all');

const chartCanvas = ref<HTMLCanvasElement | null>(null);
let chart: Chart | null = null;

const toasts = ref<ToastItem[]>([]);
let toastId = 0;

let stompClient: Client | null = null;
let pushInitialized = false;
let refreshInFlight: Promise<boolean> | null = null;

const DEFAULT_MOOD_CATALOG: MoodOption[] = [
  { value: 'happy', emoji: '\uD83D\uDE0A', label: 'Happy' },
  { value: 'sad', emoji: '\uD83D\uDE22', label: 'Sad' },
  { value: 'angry', emoji: '\uD83D\uDE20', label: 'Angry' },
  { value: 'love', emoji: '\u2764\uFE0F', label: 'Love' },
  { value: 'excited', emoji: '\uD83E\uDD73', label: 'Excited' },
  { value: 'worried', emoji: '\uD83D\uDE1F', label: 'Worried' },
  { value: 'grateful', emoji: '\uD83D\uDE4F', label: 'Grateful' },
  { value: 'none', emoji: '\uD83D\uDCAD', label: 'Neutral' },
  { value: 'hug', emoji: '\uD83E\uDEC2', label: 'Hug' },
  { value: 'exhausted', emoji: '\uD83D\uDE2E\u200D\uD83D\uDCA8', label: 'Exhausted' },
  { value: 'calm', emoji: '\uD83D\uDE0C', label: 'Calm' },
  { value: 'playful', emoji: '\uD83D\uDE1C', label: 'Playful' },
  { value: 'confused', emoji: '\uD83D\uDE15', label: 'Confused' },
  { value: 'proud', emoji: '\uD83D\uDE0E', label: 'Proud' },
  { value: 'shy', emoji: '\uD83E\uDD7A', label: 'Shy' },
  { value: 'sick', emoji: '\uD83E\uDD12', label: 'Sick' },
  { value: 'stressed', emoji: '\uD83D\uDE35', label: 'Stressed' },
  { value: 'hopeful', emoji: '\uD83C\uDF1F', label: 'Hopeful' },
  { value: 'celebrating', emoji: '\uD83C\uDF89', label: 'Celebrating' },
  { value: 'lonely', emoji: '\uD83D\uDE14', label: 'Lonely' }
];

const DEFAULT_FAVORITE_MOODS = ['love', 'hug', 'happy', 'grateful', 'excited', 'calm', 'worried', 'none'];
const moodCatalog = ref<MoodOption[]>([...DEFAULT_MOOD_CATALOG]);
const favoriteMoods = ref<string[]>([...DEFAULT_FAVORITE_MOODS]);
const maxFavoriteMoods = ref(8);

const hasPendingRequests = computed(() => pendingRequests.value.length > 0);
const moodLookup = computed<Record<string, MoodOption>>(() => {
  return moodCatalog.value.reduce<Record<string, MoodOption>>((acc, mood) => {
    acc[mood.value] = mood;
    return acc;
  }, {});
});
const newsItems: NewsItem[] = [
  {
    date: 'Feb 2026',
    title: 'Stay Logged In Improvements',
    description: 'Sessions now restore automatically with refresh tokens, and invalid sessions redirect cleanly to login.'
  },
  {
    date: 'Feb 2026',
    title: 'New No-pressure Mode',
    description: 'You can now switch dashboard cards to show only the latest event time + emoji instead of totals.'
  },
  {
    date: 'Feb 2026',
    title: 'New People Hug Emoji',
    description: 'Hug now uses the two-people emoji so it better represents connection.'
  },
  {
    date: 'Feb 2026',
    title: 'Emoji Favorites Are Configurable',
    description: 'Choose your dashboard favorites from a larger mood-emoji catalog in Profile.'
  },
  {
    date: 'Feb 2026',
    title: 'Event Log Timeline Added',
    description: 'New Event Log tab with a vertical timeline for sent and received thoughts.'
  },
  {
    date: 'Feb 2026',
    title: 'Event Log Filters',
    description: 'Choose how many events to show and filter by Sent, Received, or both.'
  },
  {
    date: 'Feb 2026',
    title: 'Improved Event Row Layout',
    description: 'Mood emoji is now aligned in front of each event line with a tooltip label.'
  },
  {
    date: 'Jan 2026',
    title: 'Profile Password Change',
    description: 'Users can change account passwords directly in the Profile tab.'
  },
  {
    date: 'Jan 2026',
    title: 'News Tab For Everyone',
    description: 'The News tab is available even when logged out, so updates are always visible.'
  }
];

const moodOptions = computed<MoodOption[]>(() => {
  const normalizedFavorites = favoriteMoods.value.filter((value) => moodLookup.value[value]);
  const source = normalizedFavorites.length ? normalizedFavorites : DEFAULT_FAVORITE_MOODS;
  return source
    .map((value) => moodLookup.value[value])
    .filter((value): value is MoodOption => Boolean(value));
});

const moodSummary = computed(() => {
  const distribution = statsData.value?.totalMoodDistribution ?? {};
  const entries = Object.entries(distribution)
    .filter(([, count]) => count > 0)
    .sort((a, b) => b[1] - a[1]);

  return entries.map(([name, count]) => {
    const mood = name.toLowerCase();
    const moodOption = moodLookup.value[mood];
    return {
      name,
      label: moodOption?.label ?? mood.charAt(0).toUpperCase() + mood.slice(1),
      emoji: getMoodEmoji(mood),
      count
    };
  });
});

onMounted(async () => {
  const hasStoredSession = isAuthenticated.value || Boolean(refreshToken.value);
  if (hasStoredSession) {
    const restored = await bootstrapSession();
    if (!restored) {
      logout(false);
      showToast('Session expired. Please log in again.', 'error');
      return;
    }
  }

  if (isAuthenticated.value) {
    await loadMoodPreferences();
    await loadDashboardPreference();
    showDashboard();
    connectWebSocket();
    // Initialize push only on native platforms and once per session.
    await setupPushNotifications();
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

function showProfile() {
  currentView.value = 'profile';
  isMenuOpen.value = false;
}

function showEventLog() {
  currentView.value = 'events';
  isMenuOpen.value = false;
  loadEventLog();
}

function showNews() {
  currentView.value = 'news';
  isMenuOpen.value = false;
}

function closeNews() {
  if (isAuthenticated.value) {
    showDashboard();
    return;
  }
  currentView.value = 'auth';
}

function showStats(connectionId: string, partnerName: string) {
  currentStatsConnectionId.value = connectionId;
  statsTitle.value = `Statistics for ${partnerName}`;
  currentView.value = 'stats';
  isMenuOpen.value = false;
  loadStats();
}

function onStatsConnectionChange() {
  const partner = partners.value.find((item) => item.id === currentStatsConnectionId.value);
  statsTitle.value = partner ? `Statistics for ${partner.partnerUsername}` : 'Statistics';
  loadStats();
}

function openStatsFromNav() {
  if (!currentStatsConnectionId.value) {
    if (partners.value.length > 0) {
      showStats(partners.value[0].id, partners.value[0].partnerUsername);
    } else {
      showToast('Select a connection to view stats.', 'info');
      showDashboard();
    }
    return;
  }
  const partner = partners.value.find((item) => item.id === currentStatsConnectionId.value);
  showStats(currentStatsConnectionId.value, partner?.partnerUsername ?? 'Statistics');
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
      persistSession(data);
      await loadMoodPreferences();
      await loadDashboardPreference();
      showDashboard();
      connectWebSocket();
      // Attempt push registration right after login.
      await setupPushNotifications();
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

function logout(callServer = true) {
  if (callServer && refreshToken.value) {
    fetch('/api/auth/logout', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refreshToken.value })
    }).catch((error) => console.error(error));
  }

  clearSessionStorage();
  token.value = '';
  refreshToken.value = '';
  currentUsername.value = '';
  disconnectWebSocket();
  pushInitialized = false;
  refreshInFlight = null;
  currentView.value = 'auth';
  partners.value = [];
  pendingRequests.value = [];
  sentRequests.value = [];
  selectedMoods.value = {};
  eventLogItems.value = [];
  profileCurrentPassword.value = '';
  profileNewPassword.value = '';
  profileConfirmPassword.value = '';
  moodCatalog.value = [...DEFAULT_MOOD_CATALOG];
  favoriteMoods.value = [...DEFAULT_FAVORITE_MOODS];
  maxFavoriteMoods.value = 8;
  dashboardDisplayMode.value = 'counts';
}

async function changePassword() {
  if (!profileCurrentPassword.value || !profileNewPassword.value || !profileConfirmPassword.value) {
    showToast('Please fill in all password fields.', 'error');
    return;
  }
  if (profileNewPassword.value !== profileConfirmPassword.value) {
    showToast('New password and confirmation do not match.', 'error');
    return;
  }
  if (profileCurrentPassword.value === profileNewPassword.value) {
    showToast('New password must be different from current password.', 'error');
    return;
  }

  isPasswordBusy.value = true;
  try {
    const res = await apiFetch('/api/users/password', {
      method: 'POST',
      body: JSON.stringify({
        currentPassword: profileCurrentPassword.value,
        newPassword: profileNewPassword.value
      })
    });
    if (!res) return;

    if (res.ok) {
      profileCurrentPassword.value = '';
      profileNewPassword.value = '';
      profileConfirmPassword.value = '';
      showToast('Password updated successfully.', 'success');
      return;
    }

    const body = (await res.json().catch(() => null)) as { error?: string } | null;
    showToast(body?.error ?? 'Failed to update password.', 'error');
  } finally {
    isPasswordBusy.value = false;
  }
}

function toggleFavoriteMood(moodValue: string) {
  const existingIndex = favoriteMoods.value.indexOf(moodValue);
  if (existingIndex >= 0) {
    if (favoriteMoods.value.length === 1) {
      showToast('Keep at least one favorite emoji.', 'error');
      return;
    }
    favoriteMoods.value = favoriteMoods.value.filter((value) => value !== moodValue);
    return;
  }

  if (favoriteMoods.value.length >= maxFavoriteMoods.value) {
    showToast(`You can choose up to ${maxFavoriteMoods.value} emojis.`, 'error');
    return;
  }

  favoriteMoods.value = [...favoriteMoods.value, moodValue];
}

async function loadMoodPreferences() {
  const res = await apiFetch('/api/users/preferences/moods');
  if (!res || !res.ok) {
    return;
  }

  const data = (await res.json()) as UserMoodPreferencesDTO;
  moodCatalog.value = data.availableMoods?.length ? data.availableMoods : [...DEFAULT_MOOD_CATALOG];
  maxFavoriteMoods.value = data.maxFavorites ?? 8;

  const validFavoriteMoods = (data.favoriteMoods ?? []).filter((mood) =>
    moodCatalog.value.some((option) => option.value === mood)
  );
  favoriteMoods.value = validFavoriteMoods.length ? validFavoriteMoods : [...DEFAULT_FAVORITE_MOODS];
}

async function loadDashboardPreference() {
  const res = await apiFetch('/api/users/preferences/dashboard');
  if (!res || !res.ok) {
    return;
  }

  const data = (await res.json()) as DashboardPreferenceDTO;
  dashboardDisplayMode.value = data.mode === 'last_event' ? 'last_event' : 'counts';
}

async function saveDashboardPreference() {
  isDashboardPreferenceBusy.value = true;
  try {
    const res = await apiFetch('/api/users/preferences/dashboard', {
      method: 'PUT',
      body: JSON.stringify({ mode: dashboardDisplayMode.value })
    });
    if (!res) return;

    if (res.ok) {
      const data = (await res.json()) as DashboardPreferenceDTO;
      dashboardDisplayMode.value = data.mode === 'last_event' ? 'last_event' : 'counts';
      showToast('Dashboard view preference saved.', 'success');
      return;
    }

    const body = (await res.json().catch(() => null)) as { error?: string } | null;
    showToast(body?.error ?? 'Failed to save dashboard view preference.', 'error');
  } finally {
    isDashboardPreferenceBusy.value = false;
  }
}

async function saveMoodPreferences() {
  isMoodPreferencesBusy.value = true;
  try {
    const res = await apiFetch('/api/users/preferences/moods', {
      method: 'PUT',
      body: JSON.stringify({ favoriteMoods: favoriteMoods.value })
    });
    if (!res) return;

    if (res.ok) {
      showToast('Emoji preferences saved.', 'success');
      return;
    }

    const body = (await res.json().catch(() => null)) as { error?: string } | null;
    showToast(body?.error ?? 'Failed to save emoji preferences.', 'error');
  } finally {
    isMoodPreferencesBusy.value = false;
  }
}

async function loadEventLog() {
  const res = await apiFetch(`/api/events?limit=${eventLogLimit.value}&direction=${eventLogDirection.value}`);
  if (!res) return;
  if (!res.ok) {
    showToast('Failed to load event log.', 'error');
    return;
  }
  eventLogItems.value = (await res.json()) as EventLogItemDTO[];
}

async function loadPartners() {
  const res = await apiFetch('/api/connections');
  if (!res) return;
  if (!res.ok) return;
  const data = (await res.json()) as ConnectionDTO[];
  partners.value = data;

  data.forEach((partner) => {
    if (!selectedMoods.value[partner.id]) {
      selectedMoods.value[partner.id] = favoriteMoods.value[0] ?? 'none';
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

function selectMood(mood: string, connectionId: string) {
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

async function setupPushNotifications() {
  if (pushInitialized) return;
  if (!Capacitor.isNativePlatform()) return;

  try {
    // Android 13+ requires runtime permission for notifications.
    const permissionStatus = await PushNotifications.checkPermissions();
    if (permissionStatus.receive !== 'granted') {
      const requestStatus = await PushNotifications.requestPermissions();
      if (requestStatus.receive !== 'granted') {
        showToast('Push notifications disabled.', 'info');
        return;
      }
    }

    // Registration event fires with the current FCM token.
    PushNotifications.addListener('registration', (token) => {
      registerPushToken(token.value);
    });

    PushNotifications.addListener('registrationError', (error) => {
      console.error('Push registration error', error);
    });

    // Show a lightweight toast for foreground notifications.
    PushNotifications.addListener('pushNotificationReceived', (notification) => {
      if (notification?.title || notification?.body) {
        showToast(`${notification.title ?? 'Notification'} ${notification.body ?? ''}`.trim(), 'info');
      }
    });

    PushNotifications.addListener('pushNotificationActionPerformed', (notification) => {
      console.log('Push action performed', notification);
    });

    await PushNotifications.register();
    pushInitialized = true;
  } catch (error) {
    console.error('Failed to initialize push notifications', error);
  }
}

async function registerPushToken(tokenValue: string) {
  if (!tokenValue) return;
  // Send token + platform to backend for storage.
  const platform = Capacitor.getPlatform();
  await apiFetch('/api/push/register', {
    method: 'POST',
    body: JSON.stringify({ token: tokenValue, platform })
  });
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

  const moodPalette = [
    '#FFD700', '#4169E1', '#FF4500', '#FF69B4', '#FFA500',
    '#9370DB', '#32CD32', '#808080', '#00B894', '#6C5CE7',
    '#2D3436', '#E17055', '#0984E3', '#00CEC9', '#FDCB6E',
    '#D63031', '#636E72', '#74B9FF', '#A29BFE', '#55EFC4'
  ];
  const moods = moodCatalog.value.map((item) => item.value);

  const datasets = moods.map((mood) => {
    const values = Object.values(statsData.value!.timeBuckets).map((bucket) => bucket[mood] ?? 0);
    const moodOption = moodLookup.value[mood];
    const colorIndex = moods.indexOf(mood) % moodPalette.length;
    return {
      label: `${getMoodEmoji(mood)} ${moodOption?.label ?? mood}`,
      data: values,
      backgroundColor: moodPalette[colorIndex],
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

function getMoodEmoji(mood: string) {
  return moodLookup.value[mood]?.emoji ?? '\uD83D\uDCAD';
}

function getMoodLabel(mood: string) {
  return moodLookup.value[mood]?.label ?? 'Neutral';
}

function formatEventTime(occurredAt: string) {
  return new Date(occurredAt).toLocaleString();
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
      const refreshed = await tryRefreshSession();
      if (refreshed) {
        const retryHeaders = new Headers(init.headers ?? {});
        retryHeaders.set('Authorization', `Bearer ${token.value}`);

        if (init.body && !retryHeaders.has('Content-Type')) {
          retryHeaders.set('Content-Type', 'application/json');
        }

        const retryRes = await fetch(input, { ...init, headers: retryHeaders });
        if (retryRes.status !== 401) {
          return retryRes;
        }
      }

      logout(false);
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

async function bootstrapSession() {
  if (!token.value && refreshToken.value) {
    return tryRefreshSession();
  }
  if (token.value && !refreshToken.value) {
    return validateAccessToken();
  }
  if (!token.value) {
    return false;
  }
  return true;
}

async function validateAccessToken() {
  if (!token.value) {
    return false;
  }

  try {
    const res = await fetch('/api/connections', {
      headers: { Authorization: `Bearer ${token.value}` }
    });
    return res.ok;
  } catch (error) {
    console.error(error);
    return true;
  }
}

async function tryRefreshSession() {
  if (!refreshToken.value) {
    return false;
  }

  if (refreshInFlight) {
    return refreshInFlight;
  }

  refreshInFlight = refreshAccessToken().finally(() => {
    refreshInFlight = null;
  });
  return refreshInFlight;
}

async function refreshAccessToken() {
  if (!refreshToken.value) {
    return false;
  }

  try {
    const res = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refreshToken.value })
    });

    if (!res.ok) {
      return false;
    }

    const data = (await res.json()) as AuthResponse;
    persistSession(data);
    return true;
  } catch (error) {
    console.error(error);
    return false;
  }
}

function persistSession(data: AuthResponse) {
  token.value = data.token;
  currentUsername.value = data.username;
  if (data.refreshToken) {
    refreshToken.value = data.refreshToken;
  }

  localStorage.setItem('token', token.value);
  localStorage.setItem('username', currentUsername.value);
  if (refreshToken.value) {
    localStorage.setItem('refreshToken', refreshToken.value);
  }
}

function clearSessionStorage() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('username');
}

interface AuthResponse {
  token: string;
  username: string;
  refreshToken?: string;
}

interface ConnectionDTO {
  id: string;
  partnerUsername: string;
  receivedClicks: number;
  sentClicks: number;
  status: string;
  lastReceivedMood?: string | null;
  lastSentMood?: string | null;
  lastReceivedAt?: string | null;
  lastSentAt?: string | null;
}

type Mood = string;

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
  value: string;
  emoji: string;
  label: string;
}

interface NewsItem {
  date: string;
  title: string;
  description: string;
}

interface EventLogItemDTO {
  connectionId: string;
  partnerUsername: string;
  direction: 'sent' | 'received';
  mood: string;
  occurredAt: string;
}

interface UserMoodPreferencesDTO {
  availableMoods: MoodOption[];
  favoriteMoods: string[];
  maxFavorites: number;
}

type DashboardDisplayMode = 'counts' | 'last_event';

interface DashboardPreferenceDTO {
  mode: DashboardDisplayMode;
}
</script>
