import { Component, OnInit, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DriverPortalService } from '../../core/services/driver-portal.service';
import { DriverStats } from '../../core/models/driver-stats.model';
import { Trip, TripStatus } from '../../core/models/trip.model';

type StatusFilter = '' | TripStatus;

@Component({
  selector: 'app-driver-dashboard',
  standalone: false,
  templateUrl: './driver-dashboard.html',
  styleUrl: './driver-dashboard.scss'
})
export class DriverDashboard implements OnInit {
  private portal = inject(DriverPortalService);
  private snack = inject(MatSnackBar);

  loading = signal(true);
  stats = signal<DriverStats | null>(null);
  trips = signal<Trip[]>([]);
  statusFilter = signal<StatusFilter>('');

  displayedColumns = ['id', 'tripDate', 'truck', 'route', 'sandType', 'tons', 'mySalary', 'status'];

  ngOnInit(): void {
    this.portal.myStats().subscribe({
      next: (s) => this.stats.set(s),
      error: (err) => this.snack.open(err?.error?.message ?? 'Failed to load stats', 'Dismiss', { duration: 4000 })
    });
    this.loadTrips();
  }

  loadTrips(): void {
    this.loading.set(true);
    this.portal.myTrips(this.statusFilter() || undefined, 0, 100).subscribe({
      next: (page) => { this.trips.set(page.content); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load trips', 'Dismiss', { duration: 4000 });
      }
    });
  }

  setStatusFilter(v: StatusFilter): void {
    this.statusFilter.set(v);
    this.loadTrips();
  }
}
