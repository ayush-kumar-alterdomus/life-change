import {
  Component,
  ChangeDetectionStrategy,
  Input,
  inject,
  computed,
  signal,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { PresenceService, UserPresence } from '../../../core/services/presence.service';

@Component({
  standalone: true,
  selector: 'app-presence-indicator',
  imports: [CommonModule],
  template: `
    <span
      class="presence-dot"
      [class.presence-dot--online]="presence().online"
      [class.presence-dot--offline]="!presence().online"
      [title]="tooltipText()"
    ></span>
  `,
  styles: [
    `
      .presence-dot {
        display: inline-block;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        &--online {
          background-color: #10b981;
        }
        &--offline {
          background-color: #9ca3af;
        }
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PresenceIndicatorComponent implements OnInit {
  @Input({ required: true }) userId!: string;

  private readonly presenceService = inject(PresenceService);
  readonly presence = signal<UserPresence>({ online: false, lastSeen: null });

  readonly tooltipText = computed(() => {
    if (this.presence().online) return 'Online';
    const lastSeen = this.presence().lastSeen;
    if (!lastSeen) return 'Offline';
    const mins = Math.round((Date.now() - lastSeen.getTime()) / 60000);
    if (mins < 1) return 'Last seen just now';
    if (mins < 60) return `Last seen ${mins}m ago`;
    return `Last seen ${Math.round(mins / 60)}h ago`;
  });

  ngOnInit(): void {
    const { presence } = this.presenceService.getUserPresence(this.userId);
    // Bridge the external signal to our local one
    // In a real app you'd use a computed or effect; simplified here
    setInterval(() => this.presence.set(presence()), 5000);
    this.presence.set(presence());
  }
}
