import { Component } from '@angular/core';
import {
  IonTabs,
  IonTabBar,
  IonTabButton,
  IonIcon,
  IonLabel,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  homeOutline,
  home,
  listOutline,
  list,
  flameOutline,
  flame,
  peopleOutline,
  people,
  personOutline,
  person,
} from 'ionicons/icons';

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel],
  template: `
    <ion-tabs>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="home">
          <ion-icon name="home-outline"></ion-icon>
          <ion-label>Home</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="quests">
          <ion-icon name="list-outline"></ion-icon>
          <ion-label>Quests</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="arc-mode">
          <ion-icon name="flame-outline"></ion-icon>
          <ion-label>Arc Mode</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="social">
          <ion-icon name="people-outline"></ion-icon>
          <ion-label>Social</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="profile">
          <ion-icon name="person-outline"></ion-icon>
          <ion-label>Profile</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  `,
  styles: [],
})
export class TabsPage {
  constructor() {
    addIcons({
      homeOutline,
      home,
      listOutline,
      list,
      flameOutline,
      flame,
      peopleOutline,
      people,
      personOutline,
      person,
    });
  }
}
