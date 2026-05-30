# ADR-001: Signal-Based State Management over NgRx

## Status

Accepted

## Context

Ascend needs reactive state management for user profiles, onboarding progress, quest data, and UI state (loading, toasts). The team evaluated:

1. **NgRx** — Full Redux pattern with actions, reducers, effects, selectors
2. **Angular Signals + lightweight stores** — Angular 17+ signals with injectable store services

## Decision

We chose **Angular Signals with injectable store services** (e.g., `UserStore`, `OnboardingStore`, `DashboardStore`).

## Rationale

| Factor | NgRx | Signals + Stores |
|--------|------|-----------------|
| Boilerplate | High (actions, reducers, effects, selectors per feature) | Minimal (signal + computed + methods) |
| Bundle size | +15-20KB (NgRx core + effects + store) | 0KB (built into Angular 17+) |
| Learning curve | Steep (Redux concepts, RxJS operators) | Low (familiar service pattern) |
| DevTools | Excellent (time-travel debugging) | Basic (signal inspector in Angular DevTools) |
| Scalability | Excellent for 50+ developers | Sufficient for small-medium teams |
| Mobile performance | Good | Better (no middleware overhead, fine-grained reactivity) |
| Change detection | Requires `async` pipe or `ngrxPush` | Automatic with `OnPush` + signals |

### Key factors for Ascend:

- **Mobile-first**: Bundle size and runtime performance matter. Signals have zero overhead.
- **Small team**: 1-3 developers. NgRx's ceremony doesn't pay off at this scale.
- **Feature isolation**: Each feature module has its own store service — no global store coupling.
- **Simplicity**: Onboarding state, quest data, and user profiles don't need time-travel debugging or complex side-effect orchestration.

## Pattern

```typescript
@Injectable({ providedIn: 'root' })
export class ExampleStore {
  private readonly _data = signal<Data | null>(null);
  readonly data = this._data.asReadonly();
  readonly derived = computed(() => /* transform _data */);

  setData(value: Data): void { this._data.set(value); }
  clearData(): void { this._data.set(null); }
}
```

Services handle async operations (API calls) and update stores. Components inject stores for reactive reads.

## Consequences

### Positive
- Minimal boilerplate — new features ship faster
- No additional dependencies
- Fine-grained reactivity with `computed()` — only affected UI re-renders
- Easy to test (just inject the store, call methods, assert signals)

### Negative
- No built-in time-travel debugging (acceptable for this app's complexity)
- No enforced unidirectional data flow (mitigated by convention: stores are readonly externally)
- If the team grows to 10+ developers, may need to revisit for stricter patterns

## Alternatives Considered

- **NgRx ComponentStore**: Lighter than full NgRx but still adds a dependency and RxJS-heavy API. Signals are simpler.
- **Elf**: Third-party reactive store. Adds dependency risk for a young library.
- **Plain BehaviorSubjects**: Works but signals integrate better with Angular's change detection and have cleaner API.
