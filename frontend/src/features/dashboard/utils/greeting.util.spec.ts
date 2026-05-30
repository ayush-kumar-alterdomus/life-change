import { getTimeBasedGreeting } from './greeting.util';

describe('getTimeBasedGreeting', () => {
  describe('morning greetings (hours 5–11)', () => {
    it('should return "Good Morning" at hour 5', () => {
      expect(getTimeBasedGreeting(null, 5)).toBe('Good Morning');
    });

    it('should return "Good Morning" at hour 11', () => {
      expect(getTimeBasedGreeting(null, 11)).toBe('Good Morning');
    });

    it('should return "Good Morning, Alex" when displayName is provided', () => {
      expect(getTimeBasedGreeting('Alex', 8)).toBe('Good Morning, Alex');
    });
  });

  describe('afternoon greetings (hours 12–16)', () => {
    it('should return "Good Afternoon" at hour 12', () => {
      expect(getTimeBasedGreeting(null, 12)).toBe('Good Afternoon');
    });

    it('should return "Good Afternoon" at hour 16', () => {
      expect(getTimeBasedGreeting(null, 16)).toBe('Good Afternoon');
    });

    it('should return "Good Afternoon, Sam" when displayName is provided', () => {
      expect(getTimeBasedGreeting('Sam', 14)).toBe('Good Afternoon, Sam');
    });
  });

  describe('evening greetings (hours 17–4)', () => {
    it('should return "Good Evening" at hour 17', () => {
      expect(getTimeBasedGreeting(null, 17)).toBe('Good Evening');
    });

    it('should return "Good Evening" at hour 23', () => {
      expect(getTimeBasedGreeting(null, 23)).toBe('Good Evening');
    });

    it('should return "Good Evening" at hour 0 (midnight)', () => {
      expect(getTimeBasedGreeting(null, 0)).toBe('Good Evening');
    });

    it('should return "Good Evening" at hour 4', () => {
      expect(getTimeBasedGreeting(null, 4)).toBe('Good Evening');
    });

    it('should return "Good Evening, Jordan" when displayName is provided', () => {
      expect(getTimeBasedGreeting('Jordan', 21)).toBe('Good Evening, Jordan');
    });
  });

  describe('displayName handling', () => {
    it('should not append anything when displayName is null', () => {
      expect(getTimeBasedGreeting(null, 10)).toBe('Good Morning');
    });

    it('should append displayName when provided', () => {
      expect(getTimeBasedGreeting('User', 10)).toBe('Good Morning, User');
    });
  });
});
