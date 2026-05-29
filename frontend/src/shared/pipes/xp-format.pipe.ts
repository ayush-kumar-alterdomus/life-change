import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ standalone: true, name: 'xpFormat', pure: true })
export class XpFormatPipe implements PipeTransform {
  transform(value: number | null | undefined, compact?: boolean): string {
    if (value == null) {
      return '0 XP';
    }

    if (value >= 1_000_000) {
      const millions = Math.floor(value / 100_000) / 10;
      return `${millions}M XP`;
    }

    if (value >= 1_000 && compact) {
      const thousands = Math.floor(value / 100) / 10;
      return `${thousands}K XP`;
    }

    return `${this.formatWithThousandSeparator(value)} XP`;
  }

  private formatWithThousandSeparator(value: number): string {
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }
}
