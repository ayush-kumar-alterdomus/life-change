# Implementation Plan: Skill Tree

## Overview

Skill node progression system tied to Arcs. Users unlock nodes that grant passive XP buffs. Enforces prerequisite ordering and supports premium skill reset with cooldown.

## Tasks

- [ ] 1. Create Skill Tree data models
  - [-] 1.1 Create skill tree tables migration
    - Create `V24__create_skill_tree_tables.sql`
    - skill_nodes: id (UUID PK), arc_id (UUID FK), name (VARCHAR NOT NULL), description (TEXT), stat_type (VARCHAR), buff_percent (DECIMAL NOT NULL), parent_node_id (UUID FK nullable — self-referencing), order_index (INT), created_at (TIMESTAMP)
    - Add `last_skill_reset_at` column to users table
    - Update user_skills to reference skill_nodes.id
  - [-] 1.2 Create DTOs
    - Create `SkillTreeResponse.java`: arcId, arcName, nodes (list of SkillNodeResponse)
    - Create `SkillNodeResponse.java`: id, name, description, statType, buffPercent, parentNodeId, unlocked, unlockedAt, children (list)
    - Create `UnlockSkillRequest.java`: skillNodeId
    - Create `SkillBuffSummary.java`: statType, totalBuffPercent (sum of all unlocked buffs for that stat)

- [ ] 2. Implement Skill Tree Service
  - [~] 2.1 Create SkillTreeService
    - Create `SkillTreeService.java` in `skilltree/service/`
    - `getSkillTree(UUID userId, UUID arcId)`:
      1. Fetch all skill_nodes for the arc
      2. Fetch user's unlocked skills
      3. Build tree structure with unlock status
      4. Return SkillTreeResponse
    - `unlockNode(UUID userId, UUID skillNodeId)`:
      1. Fetch the skill node
      2. Verify prerequisite: parent node must be unlocked (or null for root)
      3. Verify user hasn't already unlocked this node
      4. Create user_skills record (unlocked=true, unlocked_at=now())
      5. Return updated node with buff info
      6. Publish SkillUnlockedEvent
  - [~] 2.2 Create prerequisite validation
    - `validatePrerequisites(UUID userId, UUID skillNodeId)`:
      1. Fetch the node's parent_node_id
      2. If parent is null → root node, always unlockable
      3. If parent exists → check user_skills for parent unlock
      4. Throw `SkillPrerequisiteException` if parent not unlocked
    - Recursive check not needed (single parent per node)

- [ ] 3. Implement Skill Buff Calculator
  - [~] 3.1 Create SkillBuffCalculator
    - Create `SkillBuffCalculator.java` in `skilltree/service/`
    - `getActiveBuffs(UUID userId)`:
      1. Fetch all unlocked skills for user
      2. Group by stat_type
      3. Sum buff_percent per stat type
      4. Return map of StatType → totalBuffPercent
    - `calculateBoostedXp(int baseXp, StatType statType, Map<StatType, Double> buffs)`:
      1. Get buff for this stat type (default 0)
      2. Return floor(baseXp × (1 + totalBuff))
    - Integrate with XpService — call during XP calculation

- [ ] 4. Implement Skill Reset (Premium)
  - [~] 4.1 Create skill reset logic
    - `resetSkillTree(UUID userId, UUID arcId)`:
      1. Verify user is premium
      2. Check cooldown: last_skill_reset_at must be > 30 days ago (or null)
      3. Delete all user_skills for this arc
      4. Update user.last_skill_reset_at = now()
      5. Return confirmation
    - Throw `SkillResetCooldownException` if within 30 days

- [ ] 5. Create Skill Tree Controller
  - [~] 5.1 Implement REST endpoints
    - Create `SkillTreeController.java` in `skilltree/controller/`
    - GET `/api/v1/skills/tree?arcId={id}` — get skill tree for an arc
    - POST `/api/v1/skills/unlock` — unlock a skill node
    - GET `/api/v1/skills/buffs` — get active buff summary
    - POST `/api/v1/skills/reset?arcId={id}` — reset skill tree (premium only)

- [ ] 6. Write property-based tests
  - [~] 6.1 Create skill tree property tests
    - Create `SkillTreePropertyTest.java`:
      - Property 34: Prerequisite enforcement (can't unlock child without parent)
      - Property 35: Buff calculation correctness (BoostedXP = BaseXP × (1 + sum of buffs))
      - Property 36: Reset cooldown enforcement (reject if < 30 days)
    - Minimum 100 iterations per property

- [~] 7. Checkpoint - Verify skill tree
  - Integration test: unlock root node → unlock child → verify buff applies to XP
  - Integration test: attempt to unlock child without parent → rejected
  - Integration test: premium reset → verify cooldown enforced
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, or test commands. Only create/edit files. No build or test verification steps.**
- Skill trees are per-Arc — each Arc has its own tree
- Buffs are additive (multiple nodes for same stat stack)
- Prerequisite is single-parent (not DAG) — simplifies validation
- Reset is premium-only with 30-day cooldown to prevent abuse
- Buffs integrate with XpCalculator during quest completion

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "4.1"] },
    { "id": 3, "tasks": ["5.1"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["7"] }
  ]
}
```
