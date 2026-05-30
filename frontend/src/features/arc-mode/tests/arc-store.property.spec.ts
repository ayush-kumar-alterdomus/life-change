import { ArcStore } from '../store/arc.store';
import { ArcDetail } from '../models';
import { ArcType } from '@shared/enums';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

function makeArc(overrides: Partial<ArcDetail> = {}): ArcDetail {
  return {
    id: Math.random().toString(36),
    name: 'Test Arc',
    description: 'desc',
    arcType: ArcType.Warrior,
    durationDays: 60,
    phases: [],
    progressPercentage: 50,
    currentPhase: 'Beginner',
    milestones: [],
    boss: null,
    rewards: [],
    skillTreeNodes: [],
    identityTitles: {
      Beginner: 'Novice',
      Intermediate: 'Adept',
      Elite: 'Expert',
      Master: 'Legend',
    },
    questFrequency: 'DAILY',
    ...overrides,
  };
}

describe('ArcStore Property Tests', () => {
  let store: ArcStore;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    store = TestBed.inject(ArcStore);
    httpMock = TestBed.inject(HttpTestingController);
  });

  /**
   * Property 1: Arc store computed signals correctly filter arcs by category.
   */
  describe('Property 1: Filtering by category', () => {
    it('should filter prebuilt, active, and completed arcs correctly for 100 random sets', () => {
      for (let i = 0; i < 100; i++) {
        const arcs: ArcDetail[] = [];
        const prebuiltCount = Math.floor(Math.random() * 5);
        const activeCount = Math.floor(Math.random() * 5);
        const completedCount = Math.floor(Math.random() * 5);

        for (let j = 0; j < prebuiltCount; j++) {
          arcs.push(makeArc({ isPrebuilt: true }));
        }
        for (let j = 0; j < activeCount; j++) {
          arcs.push(makeArc({ startedAt: new Date(), completedAt: undefined }));
        }
        for (let j = 0; j < completedCount; j++) {
          arcs.push(makeArc({ startedAt: new Date(), completedAt: new Date() }));
        }

        // Manually set arcs via loadArcs response
        store.loadArcs();
        const req = httpMock.expectOne((r) => r.url.includes('/arcs'));
        req.flush({ data: arcs });

        expect(store.prebuiltArcs().length).toBe(prebuiltCount);
        expect(store.activeArcs().length).toBe(activeCount);
        expect(store.completedArcs().length).toBe(completedCount);
      }
    });
  });

  /**
   * Property 12: Cache-first strategy prevents redundant fetches.
   */
  describe('Property 12: Cache-first prevents redundant fetches', () => {
    it('should not fetch when arcs already loaded', () => {
      store.loadArcs();
      httpMock.expectOne((r) => r.url.includes('/arcs')).flush({ data: [makeArc()] });

      store.loadArcsIfEmpty();
      httpMock.expectNone((r) => r.url.includes('/arcs'));
    });

    it('should fetch when arcs are empty', () => {
      store.loadArcsIfEmpty();
      httpMock.expectOne((r) => r.url.includes('/arcs')).flush({ data: [] });
    });
  });

  /**
   * Property 13: Optimistic milestone update reverts on API failure.
   */
  describe('Property 13: Optimistic update reverts on failure', () => {
    it('should revert milestone completion on API error', () => {
      const arc = makeArc({
        milestones: [
          {
            id: 'm1',
            title: 'M1',
            description: '',
            completed: false,
            xpReward: 10,
            phase: 'Beginner',
            orderIndex: 0,
          },
        ],
      });

      store.loadArcDetail('test-id');
      httpMock.expectOne((r) => r.url.includes('/arcs/test-id')).flush({ data: arc });

      expect(store.selectedArcDetail()!.milestones[0].completed).toBe(false);

      store.completeMilestone('test-id', 'm1');

      // Optimistic: should be true immediately
      expect(store.selectedArcDetail()!.milestones[0].completed).toBe(true);

      // Simulate API failure
      httpMock
        .expectOne((r) => r.url.includes('/arcs/progress'))
        .flush('error', { status: 500, statusText: 'Error' });

      // Should revert
      expect(store.selectedArcDetail()!.milestones[0].completed).toBe(false);
    });
  });
});
