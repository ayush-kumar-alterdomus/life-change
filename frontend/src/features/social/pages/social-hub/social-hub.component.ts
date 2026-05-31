import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonRefresher,
  IonRefresherContent,
} from '@ionic/angular/standalone';
import { SocialStore } from '../../store/social.store';
import { LeaderboardSectionComponent } from '../../components/leaderboard-section/leaderboard-section.component';
import { GuildSectionComponent } from '../../components/guild-section/guild-section.component';
import { FriendsSectionComponent } from '../../components/friends-section/friends-section.component';
import { ChallengesSectionComponent } from '../../components/challenges-section/challenges-section.component';

@Component({
  standalone: true,
  selector: 'app-social-hub',
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    IonRefresher,
    IonRefresherContent,
    LeaderboardSectionComponent,
    GuildSectionComponent,
    FriendsSectionComponent,
    ChallengesSectionComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Social</ion-title>
      </ion-toolbar>
      <ion-toolbar>
        <ion-segment
          [value]="selectedTab()"
          (ionChange)="onTabChange($any($event).detail.value)"
          role="tablist"
        >
          <ion-segment-button value="leaderboard" role="tab" [attr.aria-selected]="selectedTab() === 'leaderboard'">
            <ion-label>Leaderboard</ion-label>
          </ion-segment-button>
          <ion-segment-button value="guild" role="tab" [attr.aria-selected]="selectedTab() === 'guild'">
            <ion-label>Guild</ion-label>
          </ion-segment-button>
          <ion-segment-button value="friends" role="tab" [attr.aria-selected]="selectedTab() === 'friends'">
            <ion-label>Friends</ion-label>
          </ion-segment-button>
          <ion-segment-button value="challenges" role="tab" [attr.aria-selected]="selectedTab() === 'challenges'">
            <ion-label>Challenges</ion-label>
          </ion-segment-button>
        </ion-segment>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <ion-refresher slot="fixed" (ionRefresh)="onRefresh($any($event))">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      <div role="tabpanel" [attr.aria-labelledby]="selectedTab()">
        @switch (selectedTab()) {
          @case ('leaderboard') {
            <app-leaderboard-section
              [leagueInfo]="store.leagueInfo()"
              [entries]="store.leaderboardEntries()"
              [loading]="store.loadingLeaderboard()"
              [error]="store.leaderboardError()"
              (loadMore)="store.loadMoreLeaderboard()"
              (retry)="store.loadLeaderboard()"
            />
          }
          @case ('guild') {
            <app-guild-section
              [myGuild]="store.myGuild()"
              [browseGuilds]="store.browseGuilds()"
              [guildLeaderboard]="store.guildLeaderboard()"
              [loading]="store.loadingGuilds()"
              [error]="store.guildsError()"
              (joinGuild)="store.joinGuild($event)"
              (viewGuild)="onViewGuild($event)"
              (createGuild)="onCreateGuild()"
              (retry)="store.loadGuilds()"
            />
          }
          @case ('friends') {
            <app-friends-section
              [friends]="store.friends()"
              [pendingRequests]="store.pendingRequests()"
              [loading]="store.loadingFriends()"
              [error]="store.friendsError()"
              (acceptRequest)="store.acceptFriendRequest($event)"
              (declineRequest)="store.removeFriend($event)"
              (removeFriend)="store.removeFriend($event)"
              (sendRequest)="store.sendFriendRequest($event)"
              (retry)="store.loadFriends()"
            />
          }
          @case ('challenges') {
            <app-challenges-section
              [challenges]="store.challenges()"
              [loading]="store.loadingChallenges()"
              [error]="store.challengesError()"
              (createChallenge)="onCreateChallenge()"
              (retry)="store.loadChallenges()"
            />
          }
        }
      </div>
    </ion-content>
  `,
})
export class SocialHubComponent implements OnInit {
  readonly store = inject(SocialStore);
  private readonly router = inject(Router);

  readonly selectedTab = signal<'leaderboard' | 'guild' | 'friends' | 'challenges'>('leaderboard');

  ngOnInit(): void {
    this.loadCurrentSection();
  }

  onTabChange(tab: string): void {
    this.selectedTab.set(tab as 'leaderboard' | 'guild' | 'friends' | 'challenges');
    this.loadCurrentSection();
  }

  onRefresh(event: { target: { complete: () => void } }): void {
    this.loadCurrentSection();
    setTimeout(() => event.target.complete(), 1500);
  }

  onViewGuild(guildId: string): void {
    this.router.navigate(['/tabs/social/guild', guildId]);
  }

  onCreateGuild(): void {
    this.router.navigate(['/tabs/social/guild/create']);
  }

  onCreateChallenge(): void {
    this.router.navigate(['/tabs/social/challenge/create']);
  }

  private loadCurrentSection(): void {
    switch (this.selectedTab()) {
      case 'leaderboard':
        this.store.loadLeaderboard();
        break;
      case 'guild':
        this.store.loadGuilds();
        break;
      case 'friends':
        this.store.loadFriends();
        break;
      case 'challenges':
        this.store.loadChallenges();
        break;
    }
  }
}
