import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-loader',
  templateUrl: './app-loader.component.html',
  styleUrls: ['./app-loader.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    '[class]': '"app-loader--" + mode() + " app-loader--" + size()',
    role: 'status',
    'aria-label': 'Loading',
  },
})
export class AppLoaderComponent {
  /** Loading mode: spinner, skeleton, or progress */
  mode = input<'spinner' | 'skeleton' | 'progress'>('spinner');

  /** Size variant */
  size = input<'small' | 'medium' | 'large'>('medium');
}
