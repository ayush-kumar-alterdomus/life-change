export const AUTH_ERROR_MESSAGES: Record<string, string> = {
  'auth/user-not-found': 'Invalid email or password. Please try again.',
  'auth/wrong-password': 'Invalid email or password. Please try again.',
  'auth/invalid-credential': 'Invalid email or password. Please try again.',
  'auth/email-already-in-use': 'An account with this email already exists. Try logging in instead.',
  'auth/weak-password': 'Password is too weak. Please meet all requirements.',
  'auth/too-many-requests': 'Too many failed attempts. Please try again later.',
  'auth/network-request-failed': 'Network error. Check your connection and try again.',
  'auth/popup-closed-by-user': '', // Silent — user cancelled
  'auth/cancelled-popup-request': '', // Silent — user cancelled
};
