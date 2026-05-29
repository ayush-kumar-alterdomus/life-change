import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * SkeletonLoaderComponent
 *
 * A reusable standalone skeleton placeholder component that displays
 * a shimmer animation while content is loading. Supports rectangle,
 * circle, and text-line shapes with configurable dimensions.
 *
 * Colors are derived from CSS custom properties for automatic
 * light/dark mode support.
 */
@Component({
  standalone: true,
  selector: 'app-skeleton-loader',
  template: `<span class="skeleton-shimmer"></span>`,
  styleUrls: ['./skeleton-loader.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    '[class]': '"skeleton-loader skeleton-loader--" + shape()',
    '[style.width]': 'resolvedWidth()',
    '[style.height]': 'resolvedHeight()',
    '[style.border-radius]': 'resolvedBorderRadius()',
    'aria-hidden': 'true',
    role: 'presentation',
  },
})
export class SkeletonLoaderComponent {
  /** Shape variant: rectangle, circle, or text-line */
  shape = input<'rectangle' | 'circle' | 'text-line'>('rectangle');

  /** Width of the skeleton element (CSS value, e.g. '100%', '200px') */
  width = input<string>('100%');

  /** Height of the skeleton element (CSS value, e.g. '16px', '2rem') */
  height = input<string>('16px');

  /** Border radius of the skeleton element (CSS value) */
  borderRadius = input<string>('4px');

  /** Resolved width — falls back to default for zero/negative values */
  resolvedWidth = computed(() => this.sanitizeDimension(this.width(), '100%'));

  /** Resolved height — falls back to default for zero/negative values */
  resolvedHeight = computed(() => this.sanitizeDimension(this.height(), '16px'));

  /** Resolved border radius — circle forces 50%, otherwise uses input or default */
  resolvedBorderRadius = computed(() => {
    if (this.shape() === 'circle') {
      return '50%';
    }
    return this.borderRadius() || '4px';
  });

  /**
   * Validates a dimension value. Falls back to the default if the value
   * is empty, zero, or negative.
   */
  private sanitizeDimension(value: string, fallback: string): string {
    if (!value || value.trim() === '') {
      return fallback;
    }

    // Extract leading numeric portion (e.g. "-10px" → -10, "0%" → 0)
    const numericMatch = value.trim().match(/^(-?\d+(\.\d+)?)/);
    if (numericMatch) {
      const num = parseFloat(numericMatch[1]);
      if (num <= 0) {
        return fallback;
      }
    }

    return value;
  }
}
