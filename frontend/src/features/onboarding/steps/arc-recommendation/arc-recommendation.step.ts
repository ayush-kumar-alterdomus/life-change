import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { IonIcon, IonButton } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  leafOutline,
  shieldOutline,
  bookOutline,
  colorPaletteOutline,
  flameOutline,
} from 'ionicons/icons';

import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { computeRecommendedArc } from '../../utils';
import { AVAILABLE_ARCS } from '../../constants';
import { ArcDefinition } from '../../models';

@Component({
  standalone: true,
  selector: 'app-arc-recommendation',
  templateUrl: './arc-recommendation.step.html',
  styleUrls: ['./arc-recommendation.step.scss'],
  imports: [IonIcon, IonButton],
})
export class ArcRecommendationComponent implements OnInit {
  private readonly onboardingService = inject(OnboardingService);
  protected readonly store = inject(OnboardingStore);

  readonly allArcs: ArcDefinition[] = AVAILABLE_ARCS;
  readonly showOverrideList = signal<boolean>(false);
  readonly recommendedArcId = signal<string | null>(null);

  readonly selectedArc = computed<ArcDefinition | null>(() => {
    const arcId = this.store.selectedArc();
    return AVAILABLE_ARCS.find((arc) => arc.id === arcId) ?? null;
  });

  constructor() {
    addIcons({
      'leaf-outline': leafOutline,
      'shield-outline': shieldOutline,
      'book-outline': bookOutline,
      'color-palette-outline': colorPaletteOutline,
      'flame-outline': flameOutline,
    });
  }

  ngOnInit(): void {
    const goals = this.store.selectedGoals();
    const difficulty = this.store.selectedDifficulty() ?? 'balanced';
    const personalityType = this.store.personalityType() ?? 'disciplined';

    const recommendedId = computeRecommendedArc(goals, difficulty, personalityType);
    this.recommendedArcId.set(recommendedId);
    this.onboardingService.setArc(recommendedId);
  }

  beginArc(): void {
    this.onboardingService.advanceStep();
  }

  showOverride(): void {
    this.showOverrideList.set(true);
  }

  selectArc(arc: ArcDefinition): void {
    this.onboardingService.setArc(arc.id);
    this.showOverrideList.set(false);
  }

  isRecommended(arcId: string): boolean {
    return arcId === this.recommendedArcId();
  }
}
