import { trigger, transition, style, animate, query, stagger } from '@angular/animations';

export const flyUpAnimation = trigger('flyUp', [
  transition(':enter', [
    style({ transform: 'translateY(40px)', opacity: 0 }),
    animate('200ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
  ]),
]);

export const slideUpAnimation = trigger('slideUp', [
  transition(':enter', [
    style({ transform: 'translateY(100%)', opacity: 0 }),
    animate('400ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
  ]),
]);

export const scaleInAnimation = trigger('scaleIn', [
  transition(':enter', [
    style({ transform: 'scale(0)', opacity: 0 }),
    animate('300ms ease-out', style({ transform: 'scale(1)', opacity: 1 })),
  ]),
]);

export const fadeInAnimation = trigger('fadeIn', [
  transition(':enter', [style({ opacity: 0 }), animate('200ms ease-out', style({ opacity: 1 }))]),
]);

export const fadeOutAnimation = trigger('fadeOut', [
  transition(':leave', [animate('300ms ease-out', style({ opacity: 0 }))]),
]);

export const staggeredFadeIn = trigger('staggeredFadeIn', [
  transition(':enter', [
    query(
      ':enter',
      [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        stagger('100ms', [
          animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
        ]),
      ],
      { optional: true },
    ),
  ]),
]);
