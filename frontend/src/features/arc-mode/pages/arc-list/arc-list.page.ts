import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent,
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonFab,
  IonFabButton,
  IonIcon,
} from '@ionic/angular/standalone';
import { ArcStore } from '../../store/arc.store';

@Component({
  standalone: true,
  selector: 'app-arc-list',
  imports: [
    CommonModule,
    IonContent,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    IonFab,
    IonFabButton,
    IonIcon,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-content>
      <ion-segment
        [value]="selectedTab()"
        (ionChange)="selectedTab.set($any($event).detail.value)"
        role="tablist"
      >
        <ion-segment-button value="explore"><ion-label>Explore</ion-label></ion-segment-button>
        <ion-segment-button value="my-arcs"><ion-label>My Arcs</ion-label></ion-segment-button>
        <ion-segment-button value="completed"><ion-label>Completed</ion-label></ion-segment-button>
      </ion-segment>

      @if (loading()) {
        <div class="arc-list__loading">Loading arcs...</div>
      } @else if (error()) {
        <div class="arc-list__error">
          <p>{{ error() }}</p>
          <button (click)="onRetry()">Retry</button>
        </div>
      } @else {
        <div class="arc-list__grid">
          @for (arc of currentArcs(); track arc.id) {
            <div
              class="arc-list__card"
              (click)="onArcTap(arc.id)"
              (keydown.enter)="onArcTap(arc.id)"
              tabindex="0"
              role="button"
            >
              <h3>{{ arc.name }}</h3>
              <p>{{ arc.description }}</p>
              <span>{{ arc.durationDays }} days</span>
            </div>
          } @empty {
            <div class="arc-list__empty">
              @switch (selectedTab()) {
                @case ('explore') {
                  <p>No arcs available to explore yet.</p>
                }
                @case ('my-arcs') {
                  <p>You haven't started any arcs. Explore and begin your journey!</p>
                }
                @case ('completed') {
                  <p>No completed arcs yet. Keep going!</p>
                }
              }
            </div>
          }
        </div>
      }

      <ion-fab vertical="bottom" horizontal="end" slot="fixed">
        <ion-fab-button (click)="onCreateArc()">
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>
  `,
  styles: [
    `
      .arc-list__grid {
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .arc-list__card {
        background: #1a1a1a;
        border-radius: 12px;
        padding: 16px;
        cursor: pointer;
      }
      .arc-list__card h3 {
        color: #fff;
        margin: 0 0 4px;
      }
      .arc-list__card p {
        color: #aaa;
        margin: 0 0 8px;
        font-size: 0.85rem;
      }
      .arc-list__card span {
        color: #ff9800;
        font-size: 0.8rem;
      }
      .arc-list__empty {
        text-align: center;
        padding: 48px 16px;
        color: #888;
      }
      .arc-list__loading {
        text-align: center;
        padding: 48px;
        color: #888;
      }
      .arc-list__error {
        text-align: center;
        padding: 48px;
        color: #ef4444;
      }
      .arc-list__error button {
        margin-top: 12px;
        padding: 8px 16px;
        background: #ff9800;
        border: none;
        border-radius: 8px;
        color: #fff;
      }
    `,
  ],
})
export class ArcListComponent implements OnInit {
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);

  readonly selectedTab = signal<'explore' | 'my-arcs' | 'completed'>('explore');
  readonly loading = this.arcStore.loadingList;
  readonly error = this.arcStore.listError;

  readonly currentArcs = computed(() => {
    switch (this.selectedTab()) {
      case 'explore':
        return this.arcStore.prebuiltArcs();
      case 'my-arcs':
        return this.arcStore.activeArcs();
      case 'completed':
        return this.arcStore.completedArcs();
      default:
        return [];
    }
  });

  ngOnInit(): void {
    this.arcStore.loadArcsIfEmpty();
    this.selectedTab.set(this.arcStore.activeArcs().length > 0 ? 'my-arcs' : 'explore');
  }

  onArcTap(arcId: string): void {
    this.router.navigate(['/tabs/arc-mode', arcId]);
  }

  onCreateArc(): void {
    this.router.navigate(['/tabs/arc-mode', 'create']);
  }

  onRetry(): void {
    this.arcStore.loadArcs();
  }
}
