/**
 * Type declarations for @codetrix-studio/capacitor-google-auth.
 * These are placeholder types until the package is installed for native builds.
 */
declare module '@codetrix-studio/capacitor-google-auth' {
  export interface GoogleAuthUser {
    authentication: {
      idToken: string;
      accessToken: string;
    };
    email: string;
    familyName: string;
    givenName: string;
    id: string;
    imageUrl: string;
    name: string;
    serverAuthCode: string;
  }

  export const GoogleAuth: {
    signIn(): Promise<GoogleAuthUser>;
    signOut(): Promise<void>;
    refresh(): Promise<{ accessToken: string; idToken: string }>;
    initialize(options?: {
      clientId?: string;
      scopes?: string[];
      grantOfflineAccess?: boolean;
    }): void;
  };
}
