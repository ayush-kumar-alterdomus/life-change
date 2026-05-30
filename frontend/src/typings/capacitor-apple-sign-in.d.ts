/**
 * Type declarations for @capacitor-community/apple-sign-in.
 * These are placeholder types until the package is installed for native builds.
 */
declare module '@capacitor-community/apple-sign-in' {
  export interface SignInWithAppleOptions {
    clientId: string;
    redirectURI: string;
    scopes?: string;
    state?: string;
    nonce?: string;
  }

  export interface SignInWithAppleResponse {
    response: {
      user: string;
      email: string | null;
      givenName: string | null;
      familyName: string | null;
      identityToken: string;
      authorizationCode: string;
    };
  }

  export const SignInWithApple: {
    authorize(options: SignInWithAppleOptions): Promise<SignInWithAppleResponse>;
  };
}
