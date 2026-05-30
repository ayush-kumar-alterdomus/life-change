import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { ArcType } from '@shared/enums';
import { IdentityTitleMap } from '../../models';

@Component({
  standalone: true,
  selector: 'app-identity-title',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div class="identity-title" [attr.aria-label]="ariaLabel()">
    {{ currentTitle() }}
  </div>`,
  styles: [
    `
      .identity-title {
        font-family: 'Orbitron', sans-serif;
        font-size: 1.4rem;
        text-align: center;
        background: linear-gradient(135deg, #ff9800, #a855f7);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        animation: revealIn 300ms ease-out;
      }
      @keyframes revealIn {
        from {
          opacity: 0;
          transform: scale(0.8);
        }
        to {
          opacity: 1;
          transform: scale(1);
        }
      }
    `,
  ],
})
export class IdentityTitleComponent {
  currentPhase = input.required<string>();
  titles = input.required<IdentityTitleMap>();
  arcType = input.required<ArcType>();

  currentTitle = computed(() => {
    const map = this.titles();
    return map[this.currentPhase()] ?? map['Beginner'];
  });

  ariaLabel = computed(() => `Your arc identity: ${this.currentTitle()}`);
}
