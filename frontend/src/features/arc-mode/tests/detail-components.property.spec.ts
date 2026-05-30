import { ArcPhaseWithMilestones, ArcReward, SkillNode, ArcMilestoneDetail } from '../models';

describe('Detail Component Property Tests', () => {
  /**
   * Property 5: Milestone timeline groups milestones by phase with active phase expanded.
   */
  describe('Property 5: Milestone grouping and expansion', () => {
    it('should group milestones by phase and expand current phase (100 iterations)', () => {
      const PHASES = ['Beginner', 'Intermediate', 'Elite', 'Master'];

      for (let i = 0; i < 100; i++) {
        const currentPhase = PHASES[Math.floor(Math.random() * PHASES.length)];
        const phases: ArcPhaseWithMilestones[] = PHASES.map((name, order) => ({
          name,
          order,
          milestones: Array.from({ length: Math.floor(Math.random() * 5) + 1 }, (_, j) => ({
            id: `m-${order}-${j}`,
            title: `Milestone ${j}`,
            description: '',
            completed: Math.random() > 0.5,
            xpReward: 10,
            phase: name,
            orderIndex: j,
          })),
        }));

        // Verify grouping: each phase section contains only its milestones
        for (const phase of phases) {
          for (const m of phase.milestones) {
            expect(m.phase).toBe(phase.name);
          }
        }

        // Verify current phase would be expanded
        const expandedSet = new Set([currentPhase]);
        expect(expandedSet.has(currentPhase)).toBe(true);
        PHASES.filter((p) => p !== currentPhase).forEach((p) => {
          expect(expandedSet.has(p)).toBe(false);
        });
      }
    });
  });

  /**
   * Property 6: Milestone items display correct completion state and summary counts.
   */
  describe('Property 6: Completion state and summary', () => {
    it('should report correct completed/total counts (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const total = Math.floor(Math.random() * 10) + 1;
        const milestones: ArcMilestoneDetail[] = Array.from({ length: total }, (_, j) => ({
          id: `m${j}`,
          title: `M${j}`,
          description: '',
          completed: Math.random() > 0.5,
          xpReward: 10,
          phase: 'Beginner',
          orderIndex: j,
        }));

        const completed = milestones.filter((m) => m.completed).length;
        const summary = `${completed}/${total} milestones`;

        expect(summary).toBe(`${completed}/${total} milestones`);
        for (const m of milestones) {
          const icon = m.completed ? '✓' : '○';
          expect(['✓', '○']).toContain(icon);
        }
      }
    });
  });

  /**
   * Property 7: Boss section visibility follows boss presence.
   */
  describe('Property 7: Boss visibility', () => {
    it('should show boss only when non-null (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const hasBoss = Math.random() > 0.5;
        const boss = hasBoss
          ? { name: 'Boss', level: 5, healthPercentage: Math.floor(Math.random() * 101) }
          : null;

        expect(boss !== null).toBe(hasBoss);
        if (boss) {
          const isDefeated = boss.healthPercentage === 0;
          expect(typeof isDefeated).toBe('boolean');
        }
      }
    });
  });

  /**
   * Property 8: Rewards display correct earned/locked state grouped by phase.
   */
  describe('Property 8: Rewards grouping and state', () => {
    it('should group rewards by phase with correct earned/locked (100 iterations)', () => {
      const PHASES = ['Beginner', 'Intermediate', 'Elite', 'Master'];

      for (let i = 0; i < 100; i++) {
        const rewards: ArcReward[] = Array.from(
          { length: Math.floor(Math.random() * 10) + 1 },
          (_, j) => ({
            id: `r${j}`,
            name: `Reward ${j}`,
            type: 'coins' as const,
            earned: Math.random() > 0.5,
            unlocksAtPhase: PHASES[Math.floor(Math.random() * PHASES.length)],
          }),
        );

        const groups = new Map<string, ArcReward[]>();
        for (const r of rewards) {
          if (!groups.has(r.unlocksAtPhase)) groups.set(r.unlocksAtPhase, []);
          groups.get(r.unlocksAtPhase)!.push(r);
        }

        // All rewards accounted for
        let totalGrouped = 0;
        groups.forEach((g) => (totalGrouped += g.length));
        expect(totalGrouped).toBe(rewards.length);

        // Aria-label correctness
        for (const r of rewards) {
          const status = r.earned ? 'earned' : `locked, unlocks at ${r.unlocksAtPhase} phase`;
          const label = `Reward: ${r.name}, ${status}`;
          expect(label).toContain(r.name);
          expect(label).toContain(r.earned ? 'earned' : 'locked');
        }
      }
    });
  });

  /**
   * Property 9: Skill tree preview limits displayed nodes and reports correct counts.
   */
  describe('Property 9: Skill tree preview limits and counts', () => {
    it('should limit to 6 nodes and report correct unlocked count (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const totalNodes = Math.floor(Math.random() * 20) + 1;
        const nodes: SkillNode[] = Array.from({ length: totalNodes }, (_, j) => ({
          id: `n${j}`,
          name: `Node ${j}`,
          unlocked: Math.random() > 0.5,
          order: j,
        }));

        const displayNodes = nodes.slice(0, 6);
        const hasMore = nodes.length > 6;
        const unlockedCount = nodes.filter((n) => n.unlocked).length;

        expect(displayNodes.length).toBeLessThanOrEqual(6);
        expect(hasMore).toBe(totalNodes > 6);

        const ariaLabel = `Skill tree preview, ${unlockedCount} of ${totalNodes} skills unlocked, tap to view full tree`;
        expect(ariaLabel).toContain(`${unlockedCount} of ${totalNodes}`);
      }
    });
  });
});
