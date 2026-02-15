import { expect, test } from '@playwright/test';

type AuthResponse = { token: string; username: string };

const password = 'Passw0rd!';

function uniqueUser(prefix: string) {
  const nonce = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  return `${prefix}-${nonce}`;
}

async function registerUser(request: any, username: string, userPassword: string) {
  const res = await request.post('/api/auth/register', {
    data: { username, password: userPassword }
  });
  expect(res.ok()).toBeTruthy();
}

async function loginUser(request: any, username: string, userPassword: string) {
  const res = await request.post('/api/auth/login', {
    data: { username, password: userPassword }
  });
  expect(res.ok()).toBeTruthy();
  return (await res.json()) as AuthResponse;
}

async function loginViaStorage(page: any, token: string, username: string) {
  await page.addInitScript(({ tokenValue, usernameValue }) => {
    window.localStorage.setItem('token', tokenValue);
    window.localStorage.setItem('username', usernameValue);
  }, { tokenValue: token, usernameValue: username });

  await page.goto('/');
  await expect(page.locator('#dashboard-section')).toBeVisible();
}

test('User can register and login', async ({ page }) => {
  const username = uniqueUser('user');

  await page.goto('/');
  await page.getByPlaceholder('Username').fill(username);
  await page.getByPlaceholder('Password').fill(password);
  await page.getByRole('button', { name: 'Register' }).click();
  await expect(page.locator('#toast-container')).toContainText('Registered successfully');

  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Welcome back');
  await expect(page.locator('#dashboard-section')).toBeVisible();
  await expect(page.locator('#dashboard-section')).toContainText('My People');
});

test('User can search and send a connection request', async ({ page, request }) => {
  const requester = uniqueUser('requester');
  const recipient = uniqueUser('recipient');

  await registerUser(request, requester, password);
  await registerUser(request, recipient, password);
  const auth = await loginUser(request, requester, password);

  await loginViaStorage(page, auth.token, auth.username);

  await page.locator('#header-nav').getByRole('button', { name: 'Search' }).click();
  await expect(page.locator('#search-section')).toBeVisible();

  await page.getByPlaceholder('Exact username').fill(recipient);
  await page.locator('#search-section').getByRole('button', { name: 'Search' }).click();

  const result = page.locator('#search-result').getByText(recipient, { exact: true });
  await expect(result).toBeVisible();
  await page.getByRole('button', { name: 'Send Connection Request' }).click();

  await expect(page.locator('#sent-requests')).toContainText(`Request sent to ${recipient}`);
});

test('User can accept a request and send a thought with mood', async ({ page, request }) => {
  const sender = uniqueUser('sender');
  const receiver = uniqueUser('receiver');

  await registerUser(request, sender, password);
  await registerUser(request, receiver, password);

  const senderAuth = await loginUser(request, sender, password);
  await request.post('/api/connections/request', {
    data: { username: receiver },
    headers: { Authorization: `Bearer ${senderAuth.token}` }
  });

  const receiverAuth = await loginUser(request, receiver, password);
  await loginViaStorage(page, receiverAuth.token, receiverAuth.username);

  const requestItem = page.locator('#requests-list .request-item').filter({ hasText: sender });
  await expect(requestItem).toBeVisible();
  await requestItem.getByRole('button', { name: 'Accept' }).click();

  const partnerCard = page.locator('.partner-card').filter({ hasText: sender });
  await expect(partnerCard).toBeVisible();

  const sentCount = partnerCard
    .locator('.stat-item')
    .filter({ hasText: 'Sent' })
    .locator('.stat-value');
  await expect(sentCount).toHaveText('0');

  await partnerCard.getByRole('button', { name: 'Love' }).click();
  await partnerCard.getByRole('button', { name: "I'm thinking of you!" }).click();

  await expect(page.locator('#toast-container')).toContainText('Sent a thought');
  await expect(sentCount).toHaveText('1');
});

test('User can view mood statistics for a connection', async ({ page, request }) => {
  const sender = uniqueUser('stats-sender');
  const receiver = uniqueUser('stats-receiver');

  await registerUser(request, sender, password);
  await registerUser(request, receiver, password);

  const senderAuth = await loginUser(request, sender, password);
  const receiverAuth = await loginUser(request, receiver, password);

  await request.post('/api/connections/request', {
    data: { username: receiver },
    headers: { Authorization: `Bearer ${senderAuth.token}` }
  });

  const pendingRes = await request.get('/api/connections/requests', {
    headers: { Authorization: `Bearer ${receiverAuth.token}` }
  });
  const pending = (await pendingRes.json()) as Array<{ id: string; partnerUsername: string }>;
  const match = pending.find((item) => item.partnerUsername === sender);
  expect(match).toBeTruthy();

  await request.post(`/api/connections/${match!.id}/accept`, {
    headers: { Authorization: `Bearer ${receiverAuth.token}` }
  });

  const connectionsRes = await request.get('/api/connections', {
    headers: { Authorization: `Bearer ${senderAuth.token}` }
  });
  const connections = (await connectionsRes.json()) as Array<{ id: string; partnerUsername: string }>;
  const connection = connections.find((item) => item.partnerUsername === receiver);
  expect(connection).toBeTruthy();

  await request.post(`/api/connections/${connection!.id}/think`, {
    data: { mood: 'happy' },
    headers: { Authorization: `Bearer ${senderAuth.token}` }
  });

  await loginViaStorage(page, receiverAuth.token, receiverAuth.username);

  const partnerCard = page.locator('.partner-card').filter({ hasText: sender });
  await expect(partnerCard).toBeVisible();
  await partnerCard.getByRole('button', { name: 'Stats' }).click();

  await expect(page.locator('#stats-section')).toBeVisible();
  await expect(page.locator('#stats-title')).toContainText(sender);

  const moodItem = page.locator('#mood-summary .mood-item').filter({ hasText: 'Happy' });
  await expect(moodItem).toBeVisible();
  await expect(moodItem.locator('.mood-count')).toHaveText('1');
});

test('User can change password from profile', async ({ page }) => {
  const username = uniqueUser('profile-user');
  const oldPassword = 'Passw0rd!';
  const newPassword = 'NewPassw0rd!';

  await page.goto('/');
  await page.getByPlaceholder('Username').fill(username);
  await page.getByPlaceholder('Password').fill(oldPassword);
  await page.getByRole('button', { name: 'Register' }).click();
  await expect(page.locator('#toast-container')).toContainText('Registered successfully');

  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Welcome back');

  await page.getByRole('button', { name: 'Profile' }).first().click();
  await expect(page.locator('#profile-section')).toBeVisible();

  await page.getByPlaceholder('Current password').fill(oldPassword);
  await page.getByPlaceholder('New password', { exact: true }).fill(newPassword);
  await page.getByPlaceholder('Confirm new password').fill(newPassword);
  await page.getByRole('button', { name: 'Update password' }).click();
  await expect(page.locator('#toast-container')).toContainText('Password updated successfully');

  await page.getByRole('button', { name: 'Logout' }).click();
  await expect(page.locator('#auth-section')).toBeVisible();

  await page.getByPlaceholder('Username').fill(username);
  await page.getByPlaceholder('Password').fill(oldPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Login failed');

  await page.getByPlaceholder('Password').fill(newPassword);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('#toast-container')).toContainText('Welcome back');
});
