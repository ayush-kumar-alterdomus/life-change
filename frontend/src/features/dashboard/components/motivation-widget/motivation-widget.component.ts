import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StorageService } from '../../../../core/services/storage.service';
import {
  selectMotivationMessage,
  MOTIVATION_MESSAGES,
  LAST_MOTIVATION_INDEX_KEY,
} from '../../utils/motivation.util';

@Component({
  standalone: true,
  selector: 'app-motivation-widget',
  templateUrl: './motivation-widget.component.html',
  styleUrls: ['./motivation-widget.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class MotivationWidgetComponent implements OnInit {
  private readonly storageService = inject(StorageService);

  /** The currently displayed motivation message */
  readonly message = signal<string>('');

  async ngOnInit(): Promise<void> {
    const lastIndex = await this.storageService.get<number>(LAST_MOTIVATION_INDEX_KEY);
    const nextIndex = selectMotivationMessage(lastIndex, MOTIVATION_MESSAGES.length);

    this.message.set(MOTIVATION_MESSAGES[nextIndex]);
    await this.storageService.set(LAST_MOTIVATION_INDEX_KEY, nextIndex);
  }
}
