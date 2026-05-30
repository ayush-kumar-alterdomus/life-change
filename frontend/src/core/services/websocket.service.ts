import { Injectable, inject, signal, OnDestroy } from '@angular/core';
import { Auth } from '@angular/fire/auth';
import { Subject } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';

export interface WsMessage<T = unknown> {
  destination: string;
  body: T;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private readonly auth = inject(Auth);
  private client: Client | null = null;
  private subscriptions: StompSubscription[] = [];
  private reconnectAttempts = 0;
  private readonly maxReconnectDelay = 40000;

  readonly connected = signal(false);
  readonly messages$ = new Subject<WsMessage>();

  async connect(): Promise<void> {
    const user = this.auth.currentUser;
    if (!user) return;

    const token = await user.getIdToken();
    const wsUrl = environment.apiUrl.replace('/api/v1', '') + '/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => {
        this.connected.set(true);
        this.reconnectAttempts = 0;
        this.subscribeToUserChannels(user.uid);
      },
      onDisconnect: () => {
        this.connected.set(false);
      },
      onStompError: () => {
        this.connected.set(false);
        this.scheduleReconnect();
      },
      onWebSocketClose: () => {
        this.connected.set(false);
        this.scheduleReconnect();
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
    this.subscriptions = [];
    this.client?.deactivate();
    this.connected.set(false);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private subscribeToUserChannels(uid: string): void {
    const channels = [
      '/queue/xp',
      '/queue/level',
      '/queue/streak',
      '/queue/boss',
      '/queue/notifications',
    ];

    for (const channel of channels) {
      const sub = this.client!.subscribe(`/user/${uid}${channel}`, (msg: IMessage) => {
        this.messages$.next({ destination: channel, body: JSON.parse(msg.body) });
      });
      this.subscriptions.push(sub);
    }
  }

  private scheduleReconnect(): void {
    const delay = Math.min(5000 * Math.pow(2, this.reconnectAttempts), this.maxReconnectDelay);
    this.reconnectAttempts++;
    setTimeout(() => this.connect(), delay);
  }
}
