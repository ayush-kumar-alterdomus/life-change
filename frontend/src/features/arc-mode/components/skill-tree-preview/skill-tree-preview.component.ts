import { Component, ChangeDetectionStrategy, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkillNode } from '../../models';

@Component({
  standalone: true,
  selector: 'app-skill-tree-preview',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="skill-preview"
      [attr.aria-label]="ariaLabel()"
      (click)="onTap()"
      (keydown.enter)="onTap()"
      tabindex="0"
      role="button"
    >
      <h2>Skill Tree</h2>
      <div class="skill-preview__nodes">
        @for (node of displayNodes(); track node.id) {
          <div class="skill-preview__node" [class.skill-preview__node--unlocked]="node.unlocked">
            {{ node.name }}
          </div>
        }
        @if (hasMore()) {
          <div class="skill-preview__more">+{{ nodes().length - 6 }} more</div>
        }
      </div>
    </div>
  `,
  styles: [
    `
      .skill-preview {
        padding: 16px;
        cursor: pointer;
      }
      .skill-preview h2 {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .skill-preview__nodes {
        display: flex;
        gap: 8px;
        overflow-x: auto;
      }
      .skill-preview__node {
        padding: 8px 12px;
        border-radius: 8px;
        background: #333;
        color: #b0b0b0;
        font-size: 0.75rem;
        white-space: nowrap;
      }
      .skill-preview__node--unlocked {
        background: #ff9800;
        color: #fff;
        box-shadow: 0 0 6px #ff9800;
      }
      .skill-preview__more {
        padding: 8px;
        color: #888;
        font-size: 0.75rem;
      }
    `,
  ],
})
export class SkillTreePreviewComponent {
  nodes = input.required<SkillNode[]>();
  navigate = output<void>();

  displayNodes = computed(() => this.nodes().slice(0, 6));
  hasMore = computed(() => this.nodes().length > 6);
  unlockedCount = computed(() => this.nodes().filter((n) => n.unlocked).length);

  ariaLabel = computed(
    () =>
      `Skill tree preview, ${this.unlockedCount()} of ${this.nodes().length} skills unlocked, tap to view full tree`,
  );

  onTap(): void {
    this.navigate.emit();
  }
}
