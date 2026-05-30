import { Injectable, inject, OnDestroy, signal } from '@angular/core';
import {
  Firestore,
  doc,
  setDoc,
  onSnapshot,
  serverTimestamp,
  Unsubscribe,
} from '@angular/fire/firestore';
import { Auth, onAuthStateChanged } from '@angular/fire/auth';

export interface UserPresence {
  online: boolean;
  lastSeen: Date | null;
}

@Injectable({ providedIn: 'root' })
export class PresenceService implements OnDestroy {
  private readonly firestore = inject(Firestore);
  private readonly auth = inject(Auth);
  private unsubscribeAuth: Unsubscribe | null = null;
  private currentUserId: string | null = null;

  constructor() {
    this.unsubscribeAuth = onAuthStateChanged(this.auth, (user) => {
      if (user) {
        this.currentUserId = user.uid;
        this.setOnline();
        this.setupVisibilityListener();
      } else {
        this.currentUserId = null;
      }
    });
  }

  ngOnDestroy(): void {
    this.setOffline();
    this.unsubscribeAuth?.();
  }

  getUserPresence(userId: string): { presence: ReturnType<typeof signal<UserPresence>> } {
    const presence = signal<UserPresence>({ online: false, lastSeen: null });

    const presenceRef = doc(this.firestore, `presence/${userId}`);
    onSnapshot(presenceRef, (snap) => {
      if (snap.exists()) {
        const data = snap.data();
        presence.set({
          online: data['online'] ?? false,
          lastSeen: data['lastSeen']?.toDate() ?? null,
        });
      }
    });

    return { presence };
  }

  private async setOnline(): Promise<void> {
    if (!this.currentUserId) return;
    const presenceRef = doc(this.firestore, `presence/${this.currentUserId}`);
    await setDoc(presenceRef, { online: true, lastSeen: serverTimestamp() }, { merge: true });
  }

  private async setOffline(): Promise<void> {
    if (!this.currentUserId) return;
    const presenceRef = doc(this.firestore, `presence/${this.currentUserId}`);
    await setDoc(presenceRef, { online: false, lastSeen: serverTimestamp() }, { merge: true });
  }

  private setupVisibilityListener(): void {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.setOnline();
      } else {
        this.setOffline();
      }
    });
  }
}
