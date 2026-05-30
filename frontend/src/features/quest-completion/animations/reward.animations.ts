import { trigger, transition, style, animate, keyframes } from '@angular/animations';

export const xpFloatAnimation = trigger('xpFloat', [
  transition(':enter', [
    style({ opacity: 1, transform: 'translateY(0) scale(1)' }),
    animate('500ms ease-out', style({ opacity: 0, transform: 'translateY(-60px) scale(1.2)' })),
  ]),
]);

export const glowAnimation = trigger('glow', [
  transition(':enter', [
    animate('600ms ease-out', keyframes([
      style({ boxShadow: '0 0 0px #FF9800', offset: 0 }),
      style({ boxShadow: '0 0 20px #FF9800', offset: 0.5 }),
      style({ boxShadow: '0 0 0px #FF9800', offset: 1 }),
    ])),
  ]),
]);

export const slideUpAnimation = trigger('slideUp', [
  transition(':enter', [
    style({ transform: 'translateY(100%)' }),
    animate('300ms ease-out', style({ transform: 'translateY(0)' })),
  ]),
  transition(':leave', [
    animate('200ms ease-in', style({ transform: 'translateY(100%)' })),
  ]),
]);
