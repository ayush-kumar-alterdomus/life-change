import { Injectable, inject, signal, computed } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ArcService } from '../services/arc.service';
import { ArcDetail, CreateArcPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class ArcStore {
  private readonly arcService = inject(ArcService);

  private readonly _arcs = signal<ArcDetail[]>([]);
  private readonly _selectedDetail = signal<ArcDetail | null>(null);

  readonly loadingList = signal(false);
  readonly loadingDetail = signal(false);
  readonly loadingCreate = signal(false);

  readonly listError = signal<string | null>(null);
  readonly detailError = signal<string | null>(null);
  readonly createError = signal<string | null>(null);

  readonly prebuiltArcs = computed(() => this._arcs().filter((a) => a.isPrebuilt));
  readonly activeArcs = computed(() => this._arcs().filter((a) => a.startedAt && !a.completedAt));
  readonly completedArcs = computed(() => this._arcs().filter((a) => !!a.completedAt));
  readonly selectedArcDetail = this._selectedDetail.asReadonly();

  loadArcsIfEmpty(): void {
    if (this._arcs().length > 0) return;
    this.loadArcs();
  }

  loadArcs(): void {
    this.loadingList.set(true);
    this.listError.set(null);
    this.arcService.getAvailableArcs().subscribe({
      next: (res) => {
        this._arcs.set(res.data);
        this.loadingList.set(false);
      },
      error: (err) => {
        this.listError.set(err.message ?? 'Failed to load arcs');
        this.loadingList.set(false);
      },
    });
  }

  loadArcDetail(id: string): void {
    this.loadingDetail.set(true);
    this.detailError.set(null);
    this._selectedDetail.set(null);
    this.arcService.getArcDetail(id).subscribe({
      next: (res) => {
        this._selectedDetail.set(res.data);
        this.loadingDetail.set(false);
      },
      error: (err) => {
        this.detailError.set(err.message ?? 'Failed to load arc detail');
        this.loadingDetail.set(false);
      },
    });
  }

  completeMilestone(arcId: string, milestoneId: string): void {
    const previous = this._selectedDetail();
    if (!previous) return;

    const updated = {
      ...previous,
      milestones: previous.milestones.map((m) =>
        m.id === milestoneId ? { ...m, completed: true } : m,
      ),
    };
    this._selectedDetail.set(updated);

    this.arcService.completeMilestone(arcId, milestoneId).subscribe({
      next: (res) => {
        this._selectedDetail.update((d) =>
          d ? { ...d, progressPercentage: res.data.progressPercent } : d,
        );
      },
      error: () => {
        this._selectedDetail.set(previous);
      },
    });
  }

  async createArc(payload: CreateArcPayload): Promise<ArcDetail | null> {
    this.loadingCreate.set(true);
    this.createError.set(null);
    try {
      const res = await firstValueFrom(this.arcService.createArc(payload));
      this._arcs.update((arcs) => [...arcs, res.data]);
      this.loadingCreate.set(false);
      return res.data;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to create arc';
      this.createError.set(message);
      this.loadingCreate.set(false);
      return null;
    }
  }
}
