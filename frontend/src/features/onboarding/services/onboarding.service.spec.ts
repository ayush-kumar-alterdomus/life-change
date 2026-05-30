import { TestBed } from '@angular/core/testing';
import { NavController } from '@ionic/angular/standalone';
import { of } from 'rxjs';
import { OnboardingService } from './onboarding.service';
import { OnboardingStore } from './onboarding.store';
import { OnboardingApiService } from './onboarding-api.service';
import { StorageService } from '@core/services/storage.service';
import { HapticService } from '@core/services/haptic.service';

describe('OnboardingService', () => {
  let service: OnboardingService;
  let mockStore: jasmine.SpyObj<OnboardingStore>;
  let mockStorage: jasmine.SpyObj<StorageService>;
  let mockApi: jasmine.SpyObj<OnboardingApiService>;
  let mockHaptic: jasmine.SpyObj<HapticService>;
  let mockNavCtrl: jasmine.SpyObj<NavController>;

  beforeEach(() => {
    mockStore = jasmine.createSpyObj('OnboardingStore', [
      'reset',
      'restore',
      'setCurrentStep',
      'setGoals',
      'setDifficulty',
      'setQuizAnswers',
      'setPersonalityType',
      'setArc',
      'setAvatar',
      'getSnapshot',
      'currentStep',
      'quizAnswers',
    ]);
    mockStore.currentStep.and.returnValue(0);
    mockStore.quizAnswers.and.returnValue([]);
    mockStore.getSnapshot.and.returnValue({
      currentStep: 0,
      selectedGoals: ['fitness'],
      selectedDifficulty: 'balanced',
      quizAnswers: [],
      personalityType: 'disciplined',
      selectedArc: 'monk',
      selectedAvatar: 'avatar1',
    });

    mockStorage = jasmine.createSpyObj('StorageService', ['get', 'set', 'remove']);
    mockStorage.get.and.returnValue(Promise.resolve(null));

    mockApi = jasmine.createSpyObj('OnboardingApiService', ['submitOnboarding']);
    mockApi.submitOnboarding.and.returnValue(of(undefined));

    mockHaptic = jasmine.createSpyObj('HapticService', ['impact']);
    mockNavCtrl = jasmine.createSpyObj('NavController', ['navigateRoot']);

    TestBed.configureTestingModule({
      providers: [
        OnboardingService,
        { provide: OnboardingStore, useValue: mockStore },
        { provide: StorageService, useValue: mockStorage },
        { provide: OnboardingApiService, useValue: mockApi },
        { provide: HapticService, useValue: mockHaptic },
        { provide: NavController, useValue: mockNavCtrl },
      ],
    });

    service = TestBed.inject(OnboardingService);
  });

  describe('initialize', () => {
    it('should reset store when no persisted state exists', async () => {
      mockStorage.get.and.returnValue(Promise.resolve(null));

      await service.initialize();

      expect(mockStore.reset).toHaveBeenCalled();
    });

    it('should restore store when valid persisted state exists', async () => {
      const savedState = {
        currentStep: 2,
        selectedGoals: ['fitness'],
        selectedDifficulty: 'balanced',
        quizAnswers: [],
        personalityType: null,
        selectedArc: null,
        selectedAvatar: null,
      };
      mockStorage.get.and.returnValue(Promise.resolve(savedState));

      await service.initialize();

      expect(mockStore.restore).toHaveBeenCalled();
    });
  });

  describe('advanceStep', () => {
    it('should increment step and persist', () => {
      mockStore.currentStep.and.returnValue(1);

      service.advanceStep();

      expect(mockStore.setCurrentStep).toHaveBeenCalledWith(2);
      expect(mockStorage.set).toHaveBeenCalled();
    });

    it('should not advance beyond max step', () => {
      mockStore.currentStep.and.returnValue(4); // MAX_STEP

      service.advanceStep();

      expect(mockStore.setCurrentStep).not.toHaveBeenCalled();
    });
  });

  describe('goBack', () => {
    it('should decrement step and persist', () => {
      mockStore.currentStep.and.returnValue(2);

      service.goBack();

      expect(mockStore.setCurrentStep).toHaveBeenCalledWith(1);
      expect(mockStorage.set).toHaveBeenCalled();
    });

    it('should not go below step 0', () => {
      mockStore.currentStep.and.returnValue(0);

      service.goBack();

      expect(mockStore.setCurrentStep).not.toHaveBeenCalled();
    });
  });

  describe('setGoals', () => {
    it('should limit goals to max 3', () => {
      service.setGoals(['a', 'b', 'c', 'd']);

      expect(mockStore.setGoals).toHaveBeenCalledWith(['a', 'b', 'c']);
    });

    it('should persist state after setting goals', () => {
      service.setGoals(['fitness']);

      expect(mockStorage.set).toHaveBeenCalled();
    });
  });

  describe('setDifficulty', () => {
    it('should update store and persist', () => {
      service.setDifficulty('hardcore');

      expect(mockStore.setDifficulty).toHaveBeenCalledWith('hardcore');
      expect(mockStorage.set).toHaveBeenCalled();
    });
  });

  describe('completeOnboarding', () => {
    it('should submit payload to API and navigate to home', (done) => {
      service.completeOnboarding().subscribe(() => {
        expect(mockApi.submitOnboarding).toHaveBeenCalled();
        expect(mockStorage.set).toHaveBeenCalledWith('onboarding_complete', true);
        expect(mockStorage.remove).toHaveBeenCalledWith('onboarding_state');
        expect(mockNavCtrl.navigateRoot).toHaveBeenCalledWith('/tabs/home');
        expect(mockStore.reset).toHaveBeenCalled();
        done();
      });
    });
  });

  describe('reset', () => {
    it('should reset store and clear storage', () => {
      service.reset();

      expect(mockStore.reset).toHaveBeenCalled();
      expect(mockStorage.remove).toHaveBeenCalledWith('onboarding_state');
    });
  });
});
