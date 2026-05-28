# Implementation Plan: Offline Sync

## Overview

Offline-first mobile experience with local action queue, server-wins conflict resolution, Firestore offline persistence, and sync status indicators.

## Tasks

- [ ] 1. Create Offline Queue (Frontend)
  - [ ] 1.1 Create offline queue service
    - Create `src/core/services/offline-queue.service.ts`
    - Store pending actions in IndexedDB (or Ionic Storage)
    - Action format: { id, type, payload, timestamp, synced }
    - `queueAction(type: string, payload: any)` — add to queue
    - `getPendingActions()` — return unsynced actions
    - `markSynced(actionId: string)` — mark as synced
    - `removeAction(actionId: string)` — remove after sync
  - [ ] 1.2 Create connectivity detection
    - Create `src/core/services/connectivity.service.ts`
    - Monitor `navigator.onLine` and Network plugin (Capacitor)
    - Expose `isOnline$` signal/observable
    - Trigger sync when transitioning from offline → online

- [ ] 2. Implement Sync Service (Frontend)
  - [ ] 2.1 Create sync orchestration
    - Create `src/core/services/sync.service.ts`
    - `syncPendingActions()`:
      1. Get all pending actions from queue
      2. Send each to backend in order (POST /api/v1/sync/batch)
      3. For accepted actions → mark synced, update local state
      4. For rejected actions → rollback optimistic state, notify user
    - Triggered on: connectivity restored, app resume, manual pull-to-refresh
  - [ ] 2.2 Create optimistic state management
    - When offline, apply actions optimistically to local state (signals/stores)
    - Track which state changes are "optimistic" (unconfirmed)
    - On sync rejection → revert optimistic changes
    - Show visual indicator for unconfirmed state

- [ ] 3. Implement Sync Endpoint (Backend)
  - [ ] 3.1 Create SyncController
    - Create `SyncController.java` in `common/controller/`
    - POST `/api/v1/sync/batch` — accepts array of queued actions
    - For each action:
      1. Validate (same as normal endpoint validation)
      2. Check idempotency (skip if already processed)
      3. Process (quest completion, etc.)
      4. Return per-action result: { actionId, status: ACCEPTED/REJECTED, reason }
    - Server-wins for conflicts (server state is authoritative)
  - [ ] 3.2 Create conflict resolution logic
    - If action conflicts with server state (e.g., quest already completed by another device):
      1. Reject the action
      2. Return current server state
      3. Client rolls back optimistic state
    - Preserve unprocessed actions in queue for retry

- [ ] 4. Configure Firestore Offline Persistence
  - [ ] 4.1 Enable Firestore offline mode
    - In Angular Firebase config, enable `persistenceEnabled: true`
    - Firestore automatically caches documents for offline access
    - Configure cache size limit (100MB default)
    - Realtime listeners continue to work offline (serve from cache)

- [ ] 5. Create Offline UI Indicators (Frontend)
  - [ ] 5.1 Create offline mode components
    - Create `src/shared/components/offline-indicator/` component
    - Show banner when offline: "You're in Offline Mode. Progress will sync later."
    - Show sync progress when reconnecting: "Syncing X actions..."
    - Show sync errors: "Some actions couldn't be synced" with retry button
    - Add to app layout (always visible when offline)

- [ ] 6. Write property-based tests
  - [ ] 6.1 Create sync property tests (backend)
    - Create `SyncPropertyTest.java`:
      - Property 14: All queued actions synced, accepted ones match server state, rejected ones rolled back
      - Property 15: Server-wins conflict resolution, pending actions preserved
    - Minimum 100 iterations per property

- [ ] 7. Checkpoint - Verify offline sync
  - Integration test: go offline → complete quest → go online → quest synced to server
  - Integration test: conflict (same quest completed on two devices) → server wins
  - Integration test: rejected action → optimistic state rolled back
  - Frontend test: offline indicator shows/hides correctly
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Offline queue uses IndexedDB for persistence across app restarts
- Server-wins is the conflict resolution strategy (simplest, most predictable)
- Firestore offline persistence provides instant UI for cached data
- Sync happens automatically on reconnect — no manual action needed
- Optimistic UI makes the app feel responsive even offline
