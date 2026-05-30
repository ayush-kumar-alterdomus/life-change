import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import {
  IonContent,
  IonButton,
  IonInput,
  IonItem,
  IonList,
  IonText,
  IonCard,
  IonCardContent,
  IonNote,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    IonContent,
    IonButton,
    IonInput,
    IonItem,
    IonList,
    IonText,
    IonCard,
    IonCardContent,
    IonNote,
  ],
  template: `
    <ion-content class="ion-padding">
      <div class="signup-container">
        <div class="logo">
          <h1>⚔️ Join Ascend</h1>
          <ion-text color="medium"><p>Create your character</p></ion-text>
        </div>

        <ion-card>
          <ion-card-content>
            <ion-list>
              <ion-item>
                <ion-input
                  label="Username"
                  labelPlacement="stacked"
                  placeholder="Your hero name"
                  [(ngModel)]="username"
                ></ion-input>
              </ion-item>
              <ion-item>
                <ion-input
                  label="Email"
                  labelPlacement="stacked"
                  type="email"
                  placeholder="your@email.com"
                  [(ngModel)]="email"
                ></ion-input>
              </ion-item>
              <ion-item>
                <ion-input
                  label="Password"
                  labelPlacement="stacked"
                  type="password"
                  placeholder="Min 8 characters"
                  [(ngModel)]="password"
                ></ion-input>
              </ion-item>
            </ion-list>

            <ion-button expand="block" (click)="signup()" class="ion-margin-top">
              Create Account
            </ion-button>

            <p *ngIf="error()" class="error-text">{{ error() }}</p>
          </ion-card-content>
        </ion-card>

        <ion-text color="medium" class="login-link">
          <p>Already have an account? <a routerLink="/auth/login">Sign In</a></p>
        </ion-text>

        <ion-note color="warning" class="dev-notice">
          🛠️ Dev Mode: Firebase not configured. Registration won't work yet.
        </ion-note>
      </div>
    </ion-content>
  `,
  styles: [
    `
      .signup-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 100%;
        max-width: 400px;
        margin: 0 auto;
      }
      .logo {
        text-align: center;
        margin-bottom: 24px;
      }
      .logo h1 {
        font-size: 28px;
        font-weight: 800;
        margin: 0;
      }
      ion-card {
        width: 100%;
      }
      .error-text {
        color: var(--ion-color-danger);
        text-align: center;
        font-size: 14px;
        margin-top: 12px;
      }
      .login-link {
        text-align: center;
        margin-top: 16px;
      }
      .login-link a {
        color: var(--ion-color-primary);
        text-decoration: none;
        font-weight: 600;
      }
      .dev-notice {
        display: block;
        text-align: center;
        margin-top: 24px;
        font-size: 12px;
      }
    `,
  ],
})
export class SignupComponent {
  username = '';
  email = '';
  password = '';
  error = signal('');

  constructor(private router: Router) {}

  signup() {
    this.error.set('Firebase not configured. Registration is not available in dev mode.');
  }
}
