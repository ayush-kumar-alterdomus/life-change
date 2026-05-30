import { Component, Input } from '@angular/core';
import { flyUpAnimation } from '../celebration-overlay/celebration-overlay.animations';

@Component({
  standalone: true,
  selector: 'app-xp-fly-up-number',
  template: `<div class="xp-fly-up" @flyUp>Level {{ level }}</div>`,
  styles: [`.xp-fly-up { font-family: 'Orbitron', sans-serif; font-size: 3rem; color: #fff; text-align: center; }`],
  animations: [flyUpAnimation],
})
export class XpFlyUpNumberComponent {
  @Input({ required: true }) level!: number;
}
