import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'de.kaviedes.thinkofyou',
  appName: 'Thinking of You',
  // Use Spring Boot's static output to avoid duplicating build artifacts.
  webDir: '../src/main/resources/static',
  server: {
    // Use http during local Android emulator testing so calls to the local backend
    // at http://10.0.2.2:8080 are not blocked as mixed content.
    androidScheme: 'http'
  }
};

export default config;
