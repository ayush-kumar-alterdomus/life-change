import { Component, ViewChild } from '@angular/core';
import {
  IonTabs,
  IonTabBar,
  IonTabButton,
  IonIcon,
  IonLabel,
} from '@ionic/angular/standalone';
import { NavController } from '@ionic/angular/standalone';
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
  templateUrl: './tabs.page.html',
  styleUrls: ['./tabs.page.scss'],
})
export class TabsPage {
  @ViewChild(IonTabs) tabs!: IonTabs;

  activeTab = 'home';

  constructor(private navCtrl: NavController) {
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

  onTabChange(event: { tab: string }): void {
    this.activeTab = event.tab;
  }

  onTabButtonClick(tab: string): void {
    if (tab === this.activeTab) {
      this.handleActiveTabTap(tab);
    }
  }

  private handleActiveTabTap(tab: string): void {
    const outlet = this.tabs?.outlet;

    if (outlet && outlet.canGoBack()) {
      this.navCtrl.navigateRoot(`/tabs/${tab}`);
    } else {
      this.scrollToTop();
    }
  }

  private scrollToTop(): void {
    const content = document.querySelector('ion-content');
    if (content) {
      (content as HTMLIonContentElement).scrollToTop(300);
    }
  }
}
