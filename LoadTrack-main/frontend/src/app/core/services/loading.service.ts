import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private inFlight = signal(0);
  readonly isLoading = computed(() => this.inFlight() > 0);

  start(): void {
    this.inFlight.update(c => c + 1);
  }

  stop(): void {
    this.inFlight.update(c => Math.max(0, c - 1));
  }
}
