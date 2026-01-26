import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'de.kaviedes.thinkofyou',
  appName: 'Thinking of You',
  // Use Spring Boot's static output to avoid duplicating build artifacts.
  webDir: '../src/main/resources/static'
};

export default config;
