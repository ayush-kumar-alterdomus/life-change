import { trigger, transition, style, animate } from '@angular/animations';

/**
 * Scale-in animation for the Perfect Day title.
 * Uses a spring-like cubic-bezier for an elastic entrance effect.
 * Hardware-accelerated via transform + opacity (Web Animations API renderer).
 *
 * Validates: Requirements 7.3, 9.4
 */
export const scaleInAnimation = trigger('scaleIn', [
  transition(':enter', [
    style({ opacity: 0, transform: 'scale(0.5)' }),
    animate(
      '400ms cubic-bezier(0.34, 1.56, 0.64, 1)',
      style({ opacity: 1, transform: 'scale(1)' })
    ),
  ]),
]);

/**
 * Fade-out animation for the Perfect Day overlay dismiss.
 * Uses ease-out timing for a smooth exit.
 * Hardware-accelerated via opacity (Web Animations API renderer).
 *
 * Validates: Requirements 7.6, 9.4
 */
export const fadeOutAnimation = trigger('fadeOut', [
  transition(':leave', [
    animate('300ms ease-out', style({ opacity: 0 })),
  ]),
]);
