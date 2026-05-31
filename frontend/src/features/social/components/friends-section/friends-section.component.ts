import { Component, ChangeDetectionStrategy, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FriendInfo } from '../../models';

@Component({
  standalone: true,
  selector: 'app-friends-section',
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <div class="skeleton">Loading friends...</div>
    } @else if (error()) {
      <div class="error"><p>{{ error() }}</p><button (click)="retry.emit()">Retry</button></div>
    } @else {
      @if (pendingRequests().length > 0) {
        <div class="pending" [attr.aria-label]="pendingRequests().length + ' pending friend requests'">
          <h3>Pending Requests ({{ pendingRequests().length }})</h3>
          @for (req of pendingRequests(); track req.userId) {
            <div class="friend-row">
              <span>{{ req.username }} · Lv.{{ req.level }}</span>
              <div class="friend-row__actions">
                <button class="btn-accept" (click)="acceptRequest.emit(req.userId)">Accept</button>
                <button class="btn-decline" (click)="declineRequest.emit(req.userId)">Decline</button>
              </div>
            </div>
          }
        </div>
      }

      <div class="search">
        <input type="text" placeholder="Search by username..." [(ngModel)]="searchQuery" />
        @if (searchQuery().length > 2) {
          <button (click)="sendRequest.emit(searchQuery())">Send Request</button>
        }
      </div>

      <h3 class="section-title">Friends ({{ friends().length }})</h3>
      @if (friends().length === 0) {
        <div class="empty">No friends yet. Search for players to connect!</div>
      } @else {
        @for (friend of friends(); track friend.userId) {
          <div class="friend-row">
            <div class="friend-info">
              <span class="dot" [class.dot--online]="friend.online"></span>
              <span>{{ friend.username }} · Lv.{{ friend.level }} · 🔥{{ friend.streak }}</span>
            </div>
            <button class="btn-remove" (click)="removeFriend.emit(friend.userId)">✕</button>
          </div>
        }
      }
    }
  `,
  styles: [`
    .pending { padding: 16px; background: #1a1a1a; border-radius: 12px; margin: 16px; }
    .pending h3 { color: #FF9800; margin: 0 0 8px; font-size: 0.9rem; }
    .friend-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; border-bottom: 1px solid #222; }
    .friend-row__actions { display: flex; gap: 8px; }
    .btn-accept { background: #10b981; border: none; color: #fff; padding: 4px 12px; border-radius: 6px; font-size: 0.8rem; }
    .btn-decline { background: #333; border: none; color: #aaa; padding: 4px 12px; border-radius: 6px; font-size: 0.8rem; }
    .btn-remove { background: none; border: none; color: #666; font-size: 1.2rem; cursor: pointer; }
    .search { display: flex; gap: 8px; padding: 16px; }
    .search input { flex: 1; padding: 10px; background: #1a1a1a; border: 1px solid #333; border-radius: 8px; color: #fff; }
    .search button { padding: 10px 16px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
    .section-title { color: #fff; padding: 0 16px; margin: 16px 0 8px; font-size: 0.95rem; }
    .friend-info { display: flex; align-items: center; gap: 8px; color: #e0e0e0; }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: #666; }
    .dot--online { background: #10b981; }
    .empty { text-align: center; padding: 32px; color: #888; }
    .skeleton, .error { text-align: center; padding: 48px; color: #888; }
    .error button { margin-top: 12px; padding: 8px 16px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
  `],
})
export class FriendsSectionComponent {
  friends = input.required<FriendInfo[]>();
  pendingRequests = input.required<FriendInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  acceptRequest = output<string>();
  declineRequest = output<string>();
  removeFriend = output<string>();
  sendRequest = output<string>();
  retry = output<void>();

  searchQuery = signal('');
}
