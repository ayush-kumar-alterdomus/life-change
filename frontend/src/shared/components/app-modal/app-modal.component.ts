import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-modal',
  imports: [CommonModule],
  templateUrl: './app-modal.component.html',
  styleUrls: ['./app-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppModalComponent {
  /** Whether the modal shows a close/dismiss button. */
  closable = input<boolean>(true);

  /** Emitted when the user dismisses the modal (backdrop tap or close button). */
  dismiss = output<void>();

  onBackdropClick(): void {
    this.dismiss.emit();
  }

  onCloseClick(): void {
    this.dismiss.emit();
  }

  /** Prevent clicks inside the content from propagating to the backdrop. */
  onContentClick(event: MouseEvent): void {
    event.stopPropagation();
  }
}
