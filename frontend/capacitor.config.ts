import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.ascend.app',
  appName: 'Ascend',
  webDir: 'www/browser',
  server: {
    androidScheme: 'https',
  },
};

export default config;
