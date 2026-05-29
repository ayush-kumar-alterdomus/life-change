import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ standalone: true, name: 'levelTitle', pure: true })
export class LevelTitlePipe implements PipeTransform {
  transform(value: number | null | undefined, format?: 'title' | 'full'): string {
    if (value == null) {
      return 'Unknown';
    }

    const title = this.getTitle(value);

    if (format === 'full') {
      return `Level ${value} \u2014 ${title}`;
    }

    return title;
  }

  private getTitle(level: number): string {
    if (level >= 76) {
      return 'Elite';
    }
    if (level >= 41) {
      return 'Advanced';
    }
    if (level >= 16) {
      return 'Intermediate';
    }
    return 'Beginner';
  }
}
