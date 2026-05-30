import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { QuestService, DailyQuestsResponse, Quest } from './quest.service';
import { ApiService } from '../../../core/services/api.service';

describe('QuestService', () => {
  let service: QuestService;
  let mockApi: jasmine.SpyObj<ApiService>;

  const mockQuest: Quest = {
    id: 'q1',
    title: 'Run 5km',
    description: 'Morning run',
    xpReward: 50,
    difficulty: 'MEDIUM',
    statType: 'STRENGTH',
    frequency: 'DAILY',
    recurring: true,
    isCustom: false,
    completed: false,
  };

  const mockDailyResponse: DailyQuestsResponse = {
    date: '2024-01-15',
    quests: [mockQuest],
    totalQuests: 1,
    completedQuests: 0,
  };

  beforeEach(() => {
    mockApi = jasmine.createSpyObj('ApiService', ['get', 'post']);

    TestBed.configureTestingModule({
      providers: [
        QuestService,
        { provide: ApiService, useValue: mockApi },
      ],
    });

    service = TestBed.inject(QuestService);
  });

  describe('getDailyQuests', () => {
    it('should call GET /quests/daily and unwrap response data', (done) => {
      mockApi.get.and.returnValue(of({ success: true, data: mockDailyResponse }));

      service.getDailyQuests().subscribe((result) => {
        expect(mockApi.get).toHaveBeenCalledWith('/quests/daily');
        expect(result.quests.length).toBe(1);
        expect(result.totalQuests).toBe(1);
        done();
      });
    });
  });

  describe('completeQuest', () => {
    it('should call POST /quests/complete with questId', (done) => {
      const mockResponse = {
        questId: 'q1',
        questTitle: 'Run 5km',
        xpEarned: 50,
        completedAt: '2024-01-15T10:00:00Z',
        message: 'Quest completed!',
      };
      mockApi.post.and.returnValue(of({ success: true, data: mockResponse }));

      service.completeQuest('q1').subscribe((result) => {
        expect(mockApi.post).toHaveBeenCalledWith('/quests/complete', { questId: 'q1' });
        expect(result.xpEarned).toBe(50);
        done();
      });
    });
  });

  describe('createQuest', () => {
    it('should call POST /quests with request payload', (done) => {
      const request = {
        title: 'New Quest',
        difficulty: 'EASY',
        xpReward: 25,
        statType: 'WISDOM',
        frequency: 'DAILY',
      };
      mockApi.post.and.returnValue(of({ success: true, data: { ...mockQuest, ...request } }));

      service.createQuest(request).subscribe((result) => {
        expect(mockApi.post).toHaveBeenCalledWith('/quests', request);
        expect(result.title).toBe('New Quest');
        done();
      });
    });
  });

  describe('getQuestById', () => {
    it('should call GET /quests/:id and return quest', (done) => {
      mockApi.get.and.returnValue(of({ success: true, data: mockQuest }));

      service.getQuestById('q1').subscribe((result) => {
        expect(mockApi.get).toHaveBeenCalledWith('/quests/q1');
        expect(result.id).toBe('q1');
        done();
      });
    });
  });
});
