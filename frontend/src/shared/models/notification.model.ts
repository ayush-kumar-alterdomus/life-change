export interface Notification {
  id: string;
  title: string;
  message: string;
  type: 'achievement' | 'quest' | 'streak' | 'guild' | 'system';
  read: boolean;
  createdAt: Date;
}
