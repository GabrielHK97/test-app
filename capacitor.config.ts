import type { CapacitorConfig } from './node_modules/@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.example.app',
  appName: 'test-app',
  webDir: '.next',
  server: {
    url: "http://192.168.1.119:3000",
    cleartext: true,
  },
};

export default config;
