import { chromium } from 'playwright';
import { spawn } from 'node:child_process';
import { mkdir, readFile } from 'node:fs/promises';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const frontendRoot = path.resolve(__dirname, '..');
const outputRoot = path.join(frontendRoot, 'play-store-assets');
const screenshotRoot = path.join(outputRoot, 'phone-screenshots');
const port = process.env.ASSET_SERVER_PORT ?? '5177';
const baseUrl = `http://127.0.0.1:${port}`;

const now = new Date('2026-04-06T13:45:00.000Z');
const isoHoursAgo = (hours) => new Date(now.getTime() - hours * 60 * 60 * 1000).toISOString();

const moodCatalog = [
  { value: 'happy', emoji: '\uD83D\uDE0A', label: 'Happy' },
  { value: 'sad', emoji: '\uD83D\uDE22', label: 'Sad' },
  { value: 'angry', emoji: '\uD83D\uDE20', label: 'Angry' },
  { value: 'love', emoji: '\u2764\uFE0F', label: 'Love' },
  { value: 'excited', emoji: '\uD83E\uDD73', label: 'Excited' },
  { value: 'worried', emoji: '\uD83D\uDE1F', label: 'Worried' },
  { value: 'grateful', emoji: '\uD83D\uDE4F', label: 'Grateful' },
  { value: 'none', emoji: '\uD83D\uDCAD', label: 'Neutral' },
  { value: 'hug', emoji: '\uD83E\uDEC2', label: 'Hug' },
  { value: 'calm', emoji: '\uD83D\uDE0C', label: 'Calm' }
];

const partners = [
  {
    id: 'conn-mara',
    partnerUsername: 'Mara',
    receivedClicks: 4,
    sentClicks: 7,
    status: 'ACCEPTED',
    lastReceivedMood: 'grateful',
    lastSentMood: 'love',
    lastReceivedAt: isoHoursAgo(2),
    lastSentAt: isoHoursAgo(0.5)
  },
  {
    id: 'conn-jules',
    partnerUsername: 'Jules',
    receivedClicks: 2,
    sentClicks: 3,
    status: 'ACCEPTED',
    lastReceivedMood: 'happy',
    lastSentMood: 'calm',
    lastReceivedAt: isoHoursAgo(7),
    lastSentAt: isoHoursAgo(4)
  }
];

const pendingRequests = [
  {
    id: 'request-sam',
    partnerUsername: 'Sam',
    receivedClicks: 0,
    sentClicks: 0,
    status: 'PENDING'
  }
];

const eventLogItems = [
  { connectionId: 'conn-mara', partnerUsername: 'Mara', direction: 'sent', mood: 'love', occurredAt: isoHoursAgo(0.5) },
  { connectionId: 'conn-mara', partnerUsername: 'Mara', direction: 'received', mood: 'grateful', occurredAt: isoHoursAgo(2) },
  { connectionId: 'conn-jules', partnerUsername: 'Jules', direction: 'sent', mood: 'calm', occurredAt: isoHoursAgo(4) },
  { connectionId: 'conn-jules', partnerUsername: 'Jules', direction: 'received', mood: 'happy', occurredAt: isoHoursAgo(7) },
  { connectionId: 'conn-mara', partnerUsername: 'Mara', direction: 'sent', mood: 'hug', occurredAt: isoHoursAgo(12) }
];

const metricBuckets = {
  [isoHoursAgo(23)]: { love: 1, hug: 0, happy: 0, grateful: 0, calm: 0 },
  [isoHoursAgo(18)]: { love: 0, hug: 1, happy: 1, grateful: 0, calm: 0 },
  [isoHoursAgo(12)]: { love: 1, hug: 1, happy: 0, grateful: 1, calm: 0 },
  [isoHoursAgo(6)]: { love: 2, hug: 0, happy: 1, grateful: 0, calm: 1 },
  [isoHoursAgo(1)]: { love: 2, hug: 1, happy: 0, grateful: 1, calm: 0 }
};

function startVite() {
  const viteBin = path.join(frontendRoot, 'node_modules', 'vite', 'bin', 'vite.js');
  const child = spawn(process.execPath, [viteBin, '--host', '127.0.0.1', '--port', port, '--strictPort'], {
    cwd: frontendRoot,
    env: { ...process.env, BROWSER: 'none' },
    stdio: ['ignore', 'pipe', 'pipe']
  });

  child.stdout.on('data', (data) => process.stdout.write(data));
  child.stderr.on('data', (data) => process.stderr.write(data));
  return child;
}

async function waitForServer(timeoutMs = 30000) {
  const startedAt = Date.now();
  while (Date.now() - startedAt < timeoutMs) {
    const isReady = await new Promise((resolve) => {
      const request = http.get(baseUrl, (response) => {
        response.resume();
        resolve(response.statusCode >= 200 && response.statusCode < 500);
      });
      request.on('error', () => resolve(false));
      request.setTimeout(1000, () => {
        request.destroy();
        resolve(false);
      });
    });

    if (isReady) return;
    await new Promise((resolve) => setTimeout(resolve, 400));
  }
  throw new Error(`Timed out waiting for ${baseUrl}`);
}

async function installMockApi(context) {
  await context.route('**/ws/**', (route) => {
    route.fulfill({ status: 204, body: '' });
  });

  await context.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const pathName = url.pathname;
    const method = route.request().method();

    if (method === 'GET' && pathName === '/api/connections') {
      return json(route, partners);
    }
    if (method === 'GET' && pathName === '/api/connections/requests') {
      return json(route, pendingRequests);
    }
    if (method === 'GET' && pathName === '/api/connections/sent') {
      return json(route, []);
    }
    if (method === 'GET' && pathName === '/api/users/preferences/moods') {
      return json(route, {
        availableMoods: moodCatalog,
        favoriteMoods: ['love', 'hug', 'happy', 'grateful', 'excited', 'calm', 'worried', 'none'],
        maxFavorites: 8
      });
    }
    if (method === 'GET' && pathName === '/api/users/preferences/dashboard') {
      return json(route, { mode: 'counts' });
    }
    if (method === 'GET' && pathName === '/api/metrics/moods') {
      return json(route, {
        timeBuckets: metricBuckets,
        totalMoodDistribution: { love: 6, hug: 3, happy: 2, grateful: 2, calm: 1 }
      });
    }
    if (method === 'GET' && pathName === '/api/events') {
      return json(route, eventLogItems);
    }
    if (method === 'POST' && pathName.endsWith('/think')) {
      return json(route, {});
    }
    if (method === 'POST' && pathName.endsWith('/accept')) {
      return json(route, {});
    }
    if (method === 'POST' && pathName === '/api/auth/logout') {
      return json(route, {});
    }

    return json(route, {});
  });
}

function json(route, data) {
  return route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify(data)
  });
}

async function preparePage(browser) {
  const context = await browser.newContext({
    viewport: { width: 412, height: 915 },
    deviceScaleFactor: 2,
    isMobile: true,
    hasTouch: true,
    locale: 'en-US'
  });

  await installMockApi(context);
  const page = await context.newPage();
  page.on('console', (message) => {
    if (['error', 'warning'].includes(message.type())) {
      console.log(`[browser ${message.type()}] ${message.text()}`);
    }
  });
  page.on('pageerror', (error) => {
    console.log(`[browser pageerror] ${error.message}`);
  });
  await page.addInitScript(() => {
    window.global = window;
    window.localStorage.setItem('token', 'store-screenshot-token');
    window.localStorage.setItem('refreshToken', 'store-screenshot-refresh-token');
    window.localStorage.setItem('username', 'Alex');
  });

  await page.goto(baseUrl, { waitUntil: 'networkidle' });
  await page.locator('#dashboard-section').waitFor({ state: 'visible' });
  await page.evaluate(() => document.fonts?.ready);
  await page.waitForTimeout(300);
  return { context, page };
}

async function screenshot(page, fileName) {
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(150);
  await page.screenshot({ path: path.join(screenshotRoot, fileName), fullPage: false });
}

async function makeScreenshots(browser) {
  const { context, page } = await preparePage(browser);

  await screenshot(page, '01-dashboard.png');

  await page.locator('.partner-card').filter({ hasText: 'Mara' }).getByRole('button', { name: 'Love' }).click();
  await screenshot(page, '02-mood-selection.png');

  await page.getByRole('button', { name: 'Stats' }).last().click();
  await page.locator('#stats-section').waitFor({ state: 'visible' });
  await page.locator('#mood-summary').waitFor({ state: 'visible' });
  await page.waitForTimeout(500);
  await screenshot(page, '03-statistics.png');

  await page.getByRole('button', { name: 'Event Log' }).click();
  await page.locator('#event-log-section').waitFor({ state: 'visible' });
  await page.waitForTimeout(300);
  await screenshot(page, '04-event-log.png');

  await page.locator('.burger-menu').click();
  await page.locator('#header-nav').getByRole('button', { name: 'Profile' }).click();
  await page.locator('#profile-section').waitFor({ state: 'visible' });
  await page.locator('#delete-account-panel').scrollIntoViewIfNeeded();
  await page.evaluate(() => window.scrollBy(0, -130));
  await page.waitForTimeout(200);
  await page.screenshot({ path: path.join(screenshotRoot, '05-profile.png'), fullPage: false });

  await page.locator('.burger-menu').click();
  await page.locator('#header-nav').getByRole('button', { name: 'News' }).click();
  await page.locator('#news-section').waitFor({ state: 'visible' });
  await page.waitForTimeout(200);
  await screenshot(page, '06-news.png');

  await context.close();
}

async function makeFeatureGraphic(browser) {
  const dashboardImage = await imageDataUrl(path.join(screenshotRoot, '01-dashboard.png'));
  const statsImage = await imageDataUrl(path.join(screenshotRoot, '03-statistics.png'));
  const eventsImage = await imageDataUrl(path.join(screenshotRoot, '04-event-log.png'));

  const page = await browser.newPage({ viewport: { width: 1024, height: 500 }, deviceScaleFactor: 1 });
  await page.setContent(`
    <!doctype html>
    <html>
      <head>
        <style>
          * { box-sizing: border-box; }
          body {
            margin: 0;
            width: 1024px;
            height: 500px;
            overflow: hidden;
            font-family: "Segoe UI", Arial, sans-serif;
            color: #301923;
            background:
              radial-gradient(circle at 10% 12%, rgba(255, 255, 255, 0.78) 0 9%, transparent 10%),
              radial-gradient(circle at 78% 18%, rgba(255, 255, 255, 0.42) 0 11%, transparent 12%),
              linear-gradient(135deg, #fff2ec 0%, #ffd8dd 47%, #eef2ff 100%);
          }
          .stage {
            position: relative;
            width: 1024px;
            height: 500px;
            padding: 58px 58px;
          }
          .copy {
            position: relative;
            z-index: 3;
            width: 430px;
          }
          .mark {
            display: inline-flex;
            width: 74px;
            height: 74px;
            align-items: center;
            justify-content: center;
            margin-bottom: 22px;
            border-radius: 50%;
            background: #ff4b5c;
            box-shadow: 0 20px 45px rgba(216, 67, 94, 0.28);
            font-size: 40px;
          }
          h1 {
            margin: 0 0 16px;
            font-size: 58px;
            line-height: 0.95;
            letter-spacing: 0;
          }
          p {
            margin: 0;
            color: #6d4b58;
            font-size: 25px;
            line-height: 1.22;
            font-weight: 600;
          }
          .phone {
            position: absolute;
            width: 210px;
            height: 432px;
            border-radius: 34px;
            padding: 10px;
            background: #1e2329;
            box-shadow: 0 28px 70px rgba(54, 35, 49, 0.26);
            overflow: hidden;
          }
          .phone img {
            width: 100%;
            height: 100%;
            border-radius: 24px;
            object-fit: cover;
            object-position: top center;
            display: block;
          }
          .phone.one {
            right: 284px;
            top: 58px;
            transform: rotate(-5deg);
            z-index: 2;
          }
          .phone.two {
            right: 139px;
            top: 34px;
            transform: rotate(4deg);
            z-index: 4;
          }
          .phone.three {
            right: 24px;
            top: 88px;
            transform: rotate(9deg);
            z-index: 1;
          }
          .pill {
            display: inline-flex;
            margin-top: 28px;
            padding: 13px 18px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.75);
            color: #d6455b;
            font-size: 18px;
            font-weight: 800;
            box-shadow: 0 10px 25px rgba(54, 35, 49, 0.08);
          }
        </style>
      </head>
      <body>
        <div class="stage">
          <div class="copy">
            <div class="mark">&#x1F4AD;</div>
            <h1>Thinking of You</h1>
            <p>A gentle way to send care, moods, and tiny check-ins to your people.</p>
            <div class="pill">No pressure. Just connection.</div>
          </div>
          <div class="phone one"><img src="${statsImage}" alt=""></div>
          <div class="phone two"><img src="${dashboardImage}" alt=""></div>
          <div class="phone three"><img src="${eventsImage}" alt=""></div>
        </div>
      </body>
    </html>
  `);
  await page.evaluate(() => document.fonts?.ready);
  await page.screenshot({ path: path.join(outputRoot, 'feature-graphic-1024x500.png'), fullPage: false });
  await page.close();
}

async function imageDataUrl(filePath) {
  const buffer = await readFile(filePath);
  return `data:image/png;base64,${buffer.toString('base64')}`;
}

async function main() {
  await mkdir(screenshotRoot, { recursive: true });

  const server = startVite();
  try {
    await waitForServer();
    const browser = await chromium.launch();
    try {
      await makeScreenshots(browser);
      await makeAppIcon(browser);
      await makeFeatureGraphic(browser);
    } finally {
      await browser.close();
    }
  } finally {
    server.kill();
  }

  const files = [
    'app-icon-512x512.png',
    'feature-graphic-1024x500.png',
    ...['01-dashboard.png', '02-mood-selection.png', '03-statistics.png', '04-event-log.png', '05-profile.png', '06-news.png']
      .map((file) => `phone-screenshots/${file}`)
  ];

  for (const file of files) {
    const buffer = await readFile(path.join(outputRoot, file));
    console.log(`${file} ${buffer.length} bytes`);
  }
}

async function makeAppIcon(browser) {
  const page = await browser.newPage({ viewport: { width: 512, height: 512 }, deviceScaleFactor: 1 });
  await page.setContent(`
    <!doctype html>
    <html>
      <head>
        <style>
          * { box-sizing: border-box; }
          body {
            margin: 0;
            width: 512px;
            height: 512px;
            overflow: hidden;
            background: transparent;
          }
          .icon {
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            width: 512px;
            height: 512px;
            border-radius: 50%;
            background: #ff4b5c;
            color: #fff;
            font-family: "Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji", sans-serif;
            font-size: 250px;
            line-height: 1;
            box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.16);
            overflow: hidden;
          }
          .mark {
            transform: translateY(8px);
          }
        </style>
      </head>
      <body>
        <div class="icon">
          <div class="mark">&#x1F4AD;</div>
        </div>
      </body>
    </html>
  `);
  await page.screenshot({ path: path.join(outputRoot, 'app-icon-512x512.png'), fullPage: false, omitBackground: true });
  await page.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
