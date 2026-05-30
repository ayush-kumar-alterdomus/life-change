import { Component, ChangeDetectionStrategy, input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-loader',
  templateUrl: './app-loader.component.html',
  styleUrls: ['./app-loader.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class AppLoaderComponent {
  /** Loading mode: spinner, skeleton, or progress */
  mode = input<'spinner' | 'skeleton' | 'progress'>('spinner');

  /** Size variant */
  size = input<'small' | 'medium' | 'large'>('medium');

  @HostBinding('class') get hostClass() {
    return `app-loader--${this.mode()} app-loader--${this.size()}`;
  }

  @HostBinding('attr.role') readonly hostRole = 'status';
  @HostBinding('attr.aria-label') readonly hostAriaLabel = 'Loading';
}
