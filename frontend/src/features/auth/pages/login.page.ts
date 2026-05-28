import { Component, CUSTOM_ELEMENTS_SCHEMA, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import {
  IonContent, IonButton, IonInput, IonItem, IonList, IonText,
  IonIcon, IonNote
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { logoGoogle, personOutline, mailOutline, lockClosedOutline } from 'ionicons/icons';

@Component({
  selector: 'app-login',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule, FormsModule, RouterModule, IonContent,
    IonButton, IonInput, IonItem, IonList, IonText, IonIcon, IonNote
  ],
  template: `
    <ion-content class="ion-padding">
      <div class="login-container">
        <div class="logo">
          <h1>⚔️ Ascend</h1>
          <ion-text color="medium">
            <p>Level up your life</p>
          </ion-text>
        </div>

        <ion-card>
          <ion-card-content>
            <ion-list>
              <ion-item>
                <ion-input
                  label="Email"
                  labelPlacement="stacked"
                  type="email"
                  placeholder="your@email.com"
                  [(ngModel)]="email">
                </ion-input>
              </ion-item>
              <ion-item>
                <ion-input
                  label="Password"
                  labelPlacement="stacked"
                  type="password"
                  placeholder="••••••••"
                  [(ngModel)]="password">
                </ion-input>
              </ion-item>
            </ion-list>

            <ion-button expand="block" (click)="login()" [disabled]="loading()" class="ion-margin-top">
              {{ loading() ? 'Signing in...' : 'Sign In' }}
            </ion-button>

            <div class="divider">
              <span>or</span>
            </div>

            <ion-button expand="block" fill="outline" (click)="loginWithGoogle()">
              <ion-icon name="logo-google" slot="start"></ion-icon>
              Continue with Google
            </ion-button>

            <ion-button expand="block" fill="clear" color="medium" (click)="continueAsGuest()">
              <ion-icon name="person-outline" slot="start"></ion-icon>
              Continue as Guest
            </ion-button>

            <p *ngIf="error()" class="error-text">{{ error() }}</p>
          </ion-card-content>
        </ion-card>

        <ion-text color="medium" class="signup-link">
          <p>Don't have an account? <a routerLink="/auth/signup">Sign Up</a></p>
        </ion-text>

        <ion-note color="warning" class="dev-notice">
          🛠️ Dev Mode: Firebase not configured. Use "Continue as Guest" to explore the app.
        </ion-note>
      </div>
    </ion-content>
  `,
  styles: [`
    .login-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100%;
      max-width: 400px;
      margin: 0 auto;
    }
    .logo { text-align: center; margin-bottom: 24px; }
    .logo h1 { font-size: 32px; font-weight: 800; margin: 0; }
    .logo p { margin: 4px 0 0; }
    ion-card { width: 100%; }
    .divider {
      text-align: center;
      margin: 16px 0;
      color: var(--ion-color-medium);
      font-size: 14px;
    }
    .error-text { color: var(--ion-color-danger); text-align: center; font-size: 14px; margin-top: 12px; }
    .signup-link { text-align: center; margin-top: 16px; }
    .signup-link a { color: var(--ion-color-primary); text-decoration: none; font-weight: 600; }
    .dev-notice { display: block; text-align: center; margin-top: 24px; font-size: 12px; }
  `],
})
export class LoginPage {
  email = '';
  password = '';
  loading = signal(false);
  error = signal('');

  constructor(private router: Router) {
    addIcons({ logoGoogle, personOutline, mailOutline, lockClosedOutline });
  }

  login() {
    this.error.set('Firebase not configured. Use "Continue as Guest" for now.');
  }

  loginWithGoogle() {
    this.error.set('Firebase not configured. Use "Continue as Guest" for now.');
  }

  continueAsGuest() {
    this.router.navigate(['/tabs/home']);
  }
}
