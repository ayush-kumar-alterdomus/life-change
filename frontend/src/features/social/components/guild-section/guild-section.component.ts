import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GuildCardComponent } from '../../../../shared/ui/guild-card/guild-card.component';
import { GuildInfo } from '../../models';

@Component({
  standalone: true,
  selector: 'app-guild-section',
  imports: [CommonModule, GuildCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <div class="skeleton">Loading guilds...</div>
    } @else if (error()) {
      <div class="error"><p>{{ error() }}</p><button (click)="retry.emit()">Retry</button></div>
    } @else {
      @if (myGuild()) {
        <div class="my-guild">
          <h3>My Guild</h3>
          <app-guild-card
            [guildName]="myGuild()!.name"
            [memberCount]="myGuild()!.memberCount"
            [guildLevel]="myGuild()!.guildLevel"
            [guildRank]="myGuild()!.guildRank ?? null"
            (view)="viewGuild.emit(myGuild()!.id)"
          />
        </div>
      } @else {
        <div class="no-guild">
          <p>You haven't joined a guild yet.</p>
          <button (click)="createGuild.emit()">Create Guild</button>
        </div>
      }
      <h3 class="section-title">Browse Guilds</h3>
      @for (guild of browseGuilds(); track guild.id) {
        <app-guild-card
          [guildName]="guild.name"
          [memberCount]="guild.memberCount"
          [guildLevel]="guild.guildLevel"
          [guildRank]="guild.guildRank ?? null"
          (join)="joinGuild.emit(guild.id)"
          (view)="viewGuild.emit(guild.id)"
        />
      }
    }
  `,
  styles: [`
    .my-guild, .no-guild { padding: 16px; }
    .my-guild h3, .section-title { color: #fff; padding: 16px 16px 8px; margin: 0; font-size: 1rem; }
    .no-guild { text-align: center; color: #888; }
    .no-guild button { margin-top: 8px; padding: 10px 20px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
    .skeleton, .error { text-align: center; padding: 48px; color: #888; }
    .error button { margin-top: 12px; padding: 8px 16px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
  `],
})
export class GuildSectionComponent {
  myGuild = input.required<GuildInfo | null>();
  browseGuilds = input.required<GuildInfo[]>();
  guildLeaderboard = input.required<GuildInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  joinGuild = output<string>();
  viewGuild = output<string>();
  createGuild = output<void>();
  retry = output<void>();
}
