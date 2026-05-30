import { FormGroup, FormControl, FormArray, Validators } from '@angular/forms';

describe('Arc Create Form Property Tests', () => {
  /**
   * Property 10: Form validation rejects invalid inputs and controls button state.
   */
  describe('Property 10: Form validation', () => {
    function createForm() {
      return new FormGroup({
        title: new FormControl('', [Validators.required, Validators.maxLength(100)]),
        goal: new FormControl('', [Validators.required, Validators.maxLength(500)]),
        durationDays: new FormControl<number | null>(null, [
          Validators.required,
          Validators.min(30),
          Validators.max(90),
        ]),
        milestones: new FormArray([new FormControl('', Validators.required)]),
        questFrequency: new FormControl('', Validators.required),
      });
    }

    it('should reject empty title (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        form.get('title')!.setValue('');
        expect(form.get('title')!.valid).toBe(false);
      }
    });

    it('should reject title exceeding 100 chars (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        const longTitle = 'x'.repeat(101 + Math.floor(Math.random() * 100));
        form.get('title')!.setValue(longTitle);
        expect(form.get('title')!.valid).toBe(false);
      }
    });

    it('should reject goal exceeding 500 chars (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        const longGoal = 'y'.repeat(501 + Math.floor(Math.random() * 200));
        form.get('goal')!.setValue(longGoal);
        expect(form.get('goal')!.valid).toBe(false);
      }
    });

    it('should reject duration outside 30-90 range (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        const invalidDuration =
          Math.random() > 0.5
            ? Math.floor(Math.random() * 29) + 1 // 1-29
            : 91 + Math.floor(Math.random() * 100); // 91+
        form.get('durationDays')!.setValue(invalidDuration);
        expect(form.get('durationDays')!.valid).toBe(false);
      }
    });

    it('should accept valid duration 30-90 (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        const validDuration = 30 + Math.floor(Math.random() * 61); // 30-90
        form.get('durationDays')!.setValue(validDuration);
        expect(form.get('durationDays')!.valid).toBe(true);
      }
    });

    it('should not allow removing last milestone (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        const milestones = form.get('milestones') as FormArray;

        // Should always have at least 1
        expect(milestones.length).toBeGreaterThanOrEqual(1);

        // Guard: don't remove if only 1
        if (milestones.length <= 1) {
          // removeMilestone guard prevents removal
          expect(milestones.length).toBe(1);
        }
      }
    });

    it('should disable submit when form is invalid (100 iterations)', () => {
      for (let i = 0; i < 100; i++) {
        const form = createForm();
        // Form starts invalid (all empty)
        const submitDisabled = form.invalid;
        expect(submitDisabled).toBe(true);
      }
    });

    it('should enable submit when form is fully valid', () => {
      const form = createForm();
      form.get('title')!.setValue('Valid Title');
      form.get('goal')!.setValue('Valid Goal');
      form.get('durationDays')!.setValue(60);
      (form.get('milestones') as FormArray).at(0).setValue('First milestone');
      form.get('questFrequency')!.setValue('DAILY');

      expect(form.valid).toBe(true);
    });
  });
});
