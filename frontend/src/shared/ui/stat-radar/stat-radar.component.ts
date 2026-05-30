import { Component, ChangeDetectionStrategy, input, computed, isDevMode, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface StatRadarPoint {
  name: string;
  value: number;
}

@Component({
  standalone: true,
  selector: 'app-stat-radar',
  templateUrl: './stat-radar.component.html',
  styleUrls: ['./stat-radar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class StatRadarComponent {
  @HostBinding('class') readonly hostClass = 'stat-radar';
  @HostBinding('attr.role') readonly hostRole = 'img';
  @HostBinding('attr.aria-label') get hostAriaLabel() { return this.ariaLabel(); }
  /** Array of stat objects with name and value (0-100 scale) */
  stats = input.required<StatRadarPoint[]>();

  /** Size of the SVG in pixels (width and height) */
  size = input<number>(200);

  /** Whether the component has enough stats to render */
  canRender = computed(() => {
    const statList = this.stats();
    if (statList.length < 3) {
      if (isDevMode()) {
        console.warn(
          '[StatRadarComponent] At least 3 stats are required to render the radar chart. Received:',
          statList.length,
        );
      }
      return false;
    }
    return true;
  });

  /** Center point of the SVG */
  center = computed(() => this.size() / 2);

  /** Radius of the chart (with padding for labels) */
  radius = computed(() => this.size() / 2 - 30);

  /** Angle step between each axis (2π / N) */
  angleStep = computed(() => (2 * Math.PI) / this.stats().length);

  /** Axis lines from center to each vertex */
  axisLines = computed(() => {
    if (!this.canRender()) return [];

    const cx = this.center();
    const cy = this.center();
    const r = this.radius();
    const step = this.angleStep();

    return this.stats().map((_, i) => {
      const angle = step * i - Math.PI / 2; // Start from top
      return {
        x2: cx + r * Math.cos(angle),
        y2: cy + r * Math.sin(angle),
      };
    });
  });

  /** Polygon points string for the filled stat area */
  polygonPoints = computed(() => {
    if (!this.canRender()) return '';

    const cx = this.center();
    const cy = this.center();
    const r = this.radius();
    const step = this.angleStep();

    return this.stats()
      .map((stat, i) => {
        const angle = step * i - Math.PI / 2;
        const value = Math.max(0, Math.min(stat.value, 100)); // Clamp 0-100
        const distance = (value / 100) * r;
        const x = cx + distance * Math.cos(angle);
        const y = cy + distance * Math.sin(angle);
        return `${x},${y}`;
      })
      .join(' ');
  });

  /** Outer boundary polygon points (100% ring) */
  outerPolygonPoints = computed(() => {
    if (!this.canRender()) return '';

    const cx = this.center();
    const cy = this.center();
    const r = this.radius();
    const step = this.angleStep();

    return this.stats()
      .map((_, i) => {
        const angle = step * i - Math.PI / 2;
        const x = cx + r * Math.cos(angle);
        const y = cy + r * Math.sin(angle);
        return `${x},${y}`;
      })
      .join(' ');
  });

  /** Label positions for each stat name */
  labels = computed(() => {
    if (!this.canRender()) return [];

    const cx = this.center();
    const cy = this.center();
    const r = this.radius();
    const step = this.angleStep();
    const labelOffset = 16;

    return this.stats().map((stat, i) => {
      const angle = step * i - Math.PI / 2;
      const x = cx + (r + labelOffset) * Math.cos(angle);
      const y = cy + (r + labelOffset) * Math.sin(angle);

      // Determine text-anchor based on position
      let anchor: 'start' | 'middle' | 'end' = 'middle';
      if (Math.cos(angle) > 0.1) anchor = 'start';
      else if (Math.cos(angle) < -0.1) anchor = 'end';

      return {
        name: stat.name,
        x,
        y,
        anchor,
      };
    });
  });

  /** Accessible label describing the chart */
  ariaLabel = computed(() => {
    if (!this.canRender()) return 'Stat radar chart: insufficient data';
    const descriptions = this.stats()
      .map((s) => `${s.name}: ${s.value}`)
      .join(', ');
    return `Stat radar chart showing ${descriptions}`;
  });
}
