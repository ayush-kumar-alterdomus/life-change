import * as fc from 'fast-check';
import { Difficulty } from './difficulty.enum';
import { StatType } from './stat-type.enum';
import { League } from './league.enum';
import { ArcType } from './arc-type.enum';
import { QuestFrequency } from './quest-frequency.enum';

/**
 * Feature: ui-design-system
 * Property 14: Enum values are lowercase strings
 *
 * For all enum definitions in the shared enums module (Difficulty, StatType,
 * League, ArcType, QuestFrequency), every enum member value SHALL be a
 * lowercase string literal matching the pattern /^[a-z]+$/.
 *
 * **Validates: Requirements 29.3**
 */
describe('Feature: ui-design-system, Property 14: Enum values are lowercase strings', () => {
  const lowercasePattern = /^[a-z]+$/;

  const allEnums: { name: string; enumObj: Record<string, string> }[] = [
    { name: 'Difficulty', enumObj: Difficulty },
    { name: 'StatType', enumObj: StatType },
    { name: 'League', enumObj: League },
    { name: 'ArcType', enumObj: ArcType },
    { name: 'QuestFrequency', enumObj: QuestFrequency },
  ];

  // Collect all enum values across all enums
  const allEnumValues: { enumName: string; key: string; value: string }[] = [];
  for (const { name, enumObj } of allEnums) {
    for (const key of Object.keys(enumObj)) {
      // TypeScript string enums don't have reverse mappings, so all keys are member names
      allEnumValues.push({ enumName: name, key, value: enumObj[key] });
    }
  }

  it('should have all enum member values matching /^[a-z]+$/ pattern', () => {
    fc.assert(
      fc.property(fc.constantFrom(...allEnumValues), ({ enumName, key, value }) => {
        expect(typeof value).toBe('string');
        expect(value).toMatch(lowercasePattern);
      }),
      { numRuns: 100 },
    );
  });

  // Individual enum tests for granular failure reporting
  describe('Difficulty enum', () => {
    it('should have all values as lowercase strings', () => {
      const values = Object.values(Difficulty);
      fc.assert(
        fc.property(fc.constantFrom(...values), (value) => {
          expect(typeof value).toBe('string');
          expect(value).toMatch(lowercasePattern);
        }),
        { numRuns: 100 },
      );
    });
  });

  describe('StatType enum', () => {
    it('should have all values as lowercase strings', () => {
      const values = Object.values(StatType);
      fc.assert(
        fc.property(fc.constantFrom(...values), (value) => {
          expect(typeof value).toBe('string');
          expect(value).toMatch(lowercasePattern);
        }),
        { numRuns: 100 },
      );
    });
  });

  describe('League enum', () => {
    it('should have all values as lowercase strings', () => {
      const values = Object.values(League);
      fc.assert(
        fc.property(fc.constantFrom(...values), (value) => {
          expect(typeof value).toBe('string');
          expect(value).toMatch(lowercasePattern);
        }),
        { numRuns: 100 },
      );
    });
  });

  describe('ArcType enum', () => {
    it('should have all values as lowercase strings', () => {
      const values = Object.values(ArcType);
      fc.assert(
        fc.property(fc.constantFrom(...values), (value) => {
          expect(typeof value).toBe('string');
          expect(value).toMatch(lowercasePattern);
        }),
        { numRuns: 100 },
      );
    });
  });

  describe('QuestFrequency enum', () => {
    it('should have all values as lowercase strings', () => {
      const values = Object.values(QuestFrequency);
      fc.assert(
        fc.property(fc.constantFrom(...values), (value) => {
          expect(typeof value).toBe('string');
          expect(value).toMatch(lowercasePattern);
        }),
        { numRuns: 100 },
      );
    });
  });
});
