import { Environment } from './environment.interface';

export const environment: Environment = {
  production: true,
  apiUrl: 'https://api.ascend-app.com/api/v1',
  firebase: {
    apiKey: 'PROD_API_KEY',
    authDomain: 'PROD_PROJECT.firebaseapp.com',
    projectId: 'PROD_PROJECT_ID',
    storageBucket: 'PROD_PROJECT.appspot.com',
    messagingSenderId: 'PROD_SENDER_ID',
    appId: 'PROD_APP_ID',
  },
};
