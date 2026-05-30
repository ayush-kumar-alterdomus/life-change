import { Component, ChangeDetectionStrategy, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonContent,
  IonBackButton,
  IonButtons,
  IonHeader,
  IonToolbar,
  IonTitle,
} from '@ionic/angular/standalone';
import { ArcStore } from '../../store/arc.store';
import { CinematicBannerComponent } from '../../components/cinematic-banner/cinematic-banner.component';
import { PhaseProgressComponent } from '../../components/phase-progress/phase-progress.component';
import { IdentityTitleComponent } from '../../components/identity-title/identity-title.component';
import { MilestoneTimelineComponent } from '../../components/milestone-timeline/milestone-timeline.component';
import { BossSectionComponent } from '../../components/boss-section/boss-section.component';
import { RewardsSectionComponent } from '../../components/rewards-section/rewards-section.component';
import { SkillTreePreviewComponent } from '../../components/skill-tree-preview/skill-tree-preview.component';
import { ArcPhaseWithMilestones } from '../../models';

@Component({
  standalone: true,
  selector: 'app-arc-detail',
  imports: [
    CommonModule,
    IonContent,
    IonBackButton,
    IonButtons,
    IonHeader,
    IonToolbar,
    IonTitle,
    CinematicBannerComponent,
    PhaseProgressComponent,
    IdentityTitleComponent,
    MilestoneTimelineComponent,
    BossSectionComponent,
    RewardsSectionComponent,
    SkillTreePreviewComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start"
          ><ion-back-button defaultHref="/arc-mode"></ion-back-button
        ></ion-buttons>
        <ion-title>{{ arcDetail()?.name ?? 'Arc Detail' }}</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      @if (loading()) {
        <div class="detail__loading">Loading...</div>
      } @else if (error()) {
        <div class="detail__error">
          <p>{{ error() }}</p>
          <button (click)="onRetry()">Retry</button>
        </div>
      } @else {
        @if (arcDetail(); as arc) {
          <app-cinematic-banner
            [arcName]="arc.name"
            [arcType]="arc.arcType"
            [currentPhase]="arc.currentPhase"
            [progressPercentage]="arc.progressPercentage"
          />
          <app-phase-progress [currentPhase]="arc.currentPhase" />
          @if (arc.identityTitles) {
            <app-identity-title
              [currentPhase]="arc.currentPhase"
              [titles]="arc.identityTitles"
              [arcType]="arc.arcType"
            />
          }
          <h2 class="detail__section-title">Milestones</h2>
          <app-milestone-timeline [phases]="getPhases(arc)" [currentPhase]="arc.currentPhase" />
          <app-boss-section [boss]="arc.boss" />
          <app-rewards-section [rewards]="arc.rewards" />
          <app-skill-tree-preview [nodes]="arc.skillTreeNodes" (navigate)="onSkillTreeTap()" />
        }
      }
    </ion-content>
  `,
  styles: [
    `
      .detail__loading,
      .detail__error {
        text-align: center;
        padding: 48px;
        color: #888;
      }
      .detail__error button {
        margin-top: 12px;
        padding: 8px 16px;
        background: #ff9800;
        border: none;
        border-radius: 8px;
        color: #fff;
      }
      .detail__section-title {
        color: #fff;
        font-size: 1.1rem;
        padding: 16px 16px 0;
        margin: 0;
      }
    `,
  ],
})
export class ArcDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);

  readonly arcDetail = this.arcStore.selectedArcDetail;
  readonly loading = this.arcStore.loadingDetail;
  readonly error = this.arcStore.detailError;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.arcStore.loadArcDetail(id);
  }

  onRetry(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.arcStore.loadArcDetail(id);
  }

  onSkillTreeTap(): void {
    const arcId = this.arcDetail()?.id;
    this.router.navigate(['/skill-tree'], { queryParams: { arcId } });
  }

  getPhases(arc: {
    milestones: {
      phase: string;
      id: string;
      title: string;
      completed: boolean;
      description: string;
      xpReward: number;
      orderIndex: number;
    }[];
  }): ArcPhaseWithMilestones[] {
    const map = new Map<string, ArcPhaseWithMilestones>();
    for (const m of arc.milestones) {
      if (!map.has(m.phase)) map.set(m.phase, { name: m.phase, order: map.size, milestones: [] });
      map.get(m.phase)!.milestones.push(m);
    }
    return Array.from(map.values());
  }
}
