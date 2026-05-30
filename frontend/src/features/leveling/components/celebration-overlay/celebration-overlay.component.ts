import {
  Component,
  inject,
  computed,
  OnInit,
  OnDestroy,
  HostBinding,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { LevelUpService } from '../../services/level-up.service';
import { GlowExplosionComponent } from '../glow-explosion/glow-explosion.component';
import { XpFlyUpNumberComponent } from '../xp-fly-up-number/xp-fly-up-number.component';
import { RewardsCardComponent } from '../rewards-card/rewards-card.component';
import { FeatureUnlockAnnouncementComponent } from '../feature-unlock-announcement/feature-unlock-announcement.component';
import { PrestigeScreenComponent } from '../prestige-screen/prestige-screen.component';
import { fadeInAnimation, fadeOutAnimation } from './celebration-overlay.animations';

@Component({
  standalone: true,
  selector: 'app-celebration-overlay',
  imports: [
    CommonModule,
    GlowExplosionComponent,
    XpFlyUpNumberComponent,
    RewardsCardComponent,
    FeatureUnlockAnnouncementComponent,
    PrestigeScreenComponent,
  ],
  animations: [fadeInAnimation, fadeOutAnimation],
  template: `
    @if (celebrationActive()) {
      <div class="overlay" @fadeIn>
        <div class="overlay__announcement" aria-live="assertive">
          Level up! You reached level {{ currentItem()?.level }}
        </div>

        @if (step() === 'glow-explosion' || step() === 'level-title' || step() === 'xp-fly-up') {
          <app-glow-explosion (progress)="onGlowProgress($event)" (complete)="onGlowComplete()" />
        }

        @if (
          step() === 'level-title' ||
          step() === 'xp-fly-up' ||
          step() === 'rewards-card' ||
          step() === 'feature-unlock' ||
          step() === 'continue-ready'
        ) {
          <app-xp-fly-up-number [level]="currentItem()!.level" />
        }

        @if (
          step() === 'rewards-card' || step() === 'feature-unlock' || step() === 'continue-ready'
        ) {
          <app-rewards-card [rewards]="currentItem()!.rewards" [level]="currentItem()!.level" />
        }

        @if (step() === 'feature-unlock' && currentItem()?.milestoneConfig) {
          <app-feature-unlock-announcement [milestoneConfig]="currentItem()!.milestoneConfig!" />
        }

        @if (step() === 'prestige-screen') {
          <app-prestige-screen />
        }

        @if (step() === 'continue-ready') {
          <button
            class="overlay__continue"
            (click)="onContinue()"
            aria-label="Continue Journey, dismiss level up celebration"
          >
            Continue Journey
          </button>
        }

        @if (queueIndicator()) {
          <div class="overlay__queue-indicator">{{ queueIndicator() }}</div>
        }
      </div>
    }
  `,
  styles: [
    `
      .overlay {
        position: fixed;
        inset: 0;
        z-index: 10000;
        background: #0a0a0a;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
      }
      .overlay__announcement {
        position: absolute;
        width: 1px;
        height: 1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
      }
      .overlay__continue {
        background: #4caf50;
        color: #fff;
        border: none;
        border-radius: 12px;
        padding: 14px 32px;
        font-size: 1rem;
        font-weight: 600;
        min-width: 44px;
        min-height: 44px;
        cursor: pointer;
      }
      .overlay__queue-indicator {
        color: #888;
        font-size: 0.8rem;
        position: absolute;
        bottom: 24px;
      }
      :host(.will-change-active) .overlay {
        will-change: transform, opacity;
      }
    `,
  ],
})
export class CelebrationOverlayComponent implements OnInit, OnDestroy {
  private readonly levelUpService = inject(LevelUpService);

  @HostBinding('attr.role') readonly role = 'dialog';
  @HostBinding('attr.aria-modal') readonly ariaModal = 'true';
  @HostBinding('attr.aria-label') readonly ariaLabel = 'Level up celebration';
  @HostBinding('class.will-change-active') get willChange() {
    return this.animating();
  }
  @HostListener('keydown.escape') onEscapeKeyPress() {
    this.onEscapeKey();
  }

  readonly step = this.levelUpService.currentStep;
  readonly currentItem = this.levelUpService.currentItem;
  readonly queueIndicator = this.levelUpService.queueIndicator;
  readonly celebrationActive = this.levelUpService.celebrationActive;

  readonly animating = computed(() => {
    const s = this.step();
    return s !== 'idle' && s !== 'continue-ready';
  });

  private previouslyFocusedElement: HTMLElement | null = null;

  ngOnInit(): void {
    this.previouslyFocusedElement = document.activeElement as HTMLElement;
  }

  ngOnDestroy(): void {
    this.previouslyFocusedElement?.focus();
  }

  onGlowProgress(progress: number): void {
    if (progress >= 0.5 && this.step() === 'glow-explosion') {
      this.levelUpService.advanceStep();
    }
    if (progress >= 0.75 && this.step() === 'level-title') {
      this.levelUpService.advanceStep();
    }
  }

  onGlowComplete(): void {
    if (this.step() === 'xp-fly-up') {
      this.levelUpService.advanceStep();
    }
  }

  onContinue(): void {
    this.levelUpService.dismiss();
  }

  onEscapeKey(): void {
    if (this.step() === 'continue-ready') {
      this.levelUpService.dismiss();
    }
  }
}
