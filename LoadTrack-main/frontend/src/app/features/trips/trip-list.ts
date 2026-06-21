import { Component, OnInit, inject, signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TripService } from '../../core/services/trip.service';
import { Trip, TripStatus } from '../../core/models/trip.model';
import { TripFormDialog } from './trip-form-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';

type StatusFilter = '' | TripStatus;

@Component({
  selector: 'app-trip-list',
  standalone: false,
  templateUrl: './trip-list.html',
  styleUrl: './trip-list.scss'
})
export class TripList implements OnInit {
  private trips = inject(TripService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'tripDate', 'truck', 'driver', 'dealer', 'sandType',
                      'tons', 'totalAmount', 'status', 'actions'];

  data = signal<Trip[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  statusFilter = signal<StatusFilter>('');

  pageSize = 10;
  pageIndex = 0;
  sort = 'id,desc';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.trips.list({
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.sort,
      status: this.statusFilter() || undefined
    }).subscribe({
      next: (page) => {
        this.data.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load trips', 'Dismiss', { duration: 4000 });
      }
    });
  }

  setStatusFilter(v: StatusFilter): void {
    this.statusFilter.set(v);
    this.pageIndex = 0;
    this.load();
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.load(); }
  onSort(s: Sort): void { this.sort = s.direction ? `${s.active},${s.direction}` : 'id,desc'; this.load(); }

  add(): void {
    const ref = this.dialog.open(TripFormDialog, { data: { mode: 'add' }, width: '720px' });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  edit(trip: Trip): void {
    if (trip.status !== 'PENDING') {
      this.snack.open('Only PENDING trips can be edited', 'Dismiss', { duration: 3000 });
      return;
    }
    const ref = this.dialog.open(TripFormDialog, { data: { mode: 'edit', trip }, width: '720px' });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  changeStatus(trip: Trip, newStatus: TripStatus): void {
    this.trips.changeStatus(trip.id, newStatus).subscribe({
      next: () => {
        this.snack.open(`Trip marked as ${newStatus}`, 'Dismiss', { duration: 2500 });
        this.load();
      },
      error: (err) => this.snack.open(err?.error?.message ?? 'Status change failed', 'Dismiss', { duration: 4000 })
    });
  }

  remove(trip: Trip): void {
    const data: ConfirmDialogData = {
      title: 'Delete Trip',
      message: `Permanently delete trip #${trip.id}? The associated payment will also be deleted.`,
      confirmText: 'Delete',
      danger: true
    };
    const ref = this.dialog.open(ConfirmDialog, { data });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.trips.delete(trip.id).subscribe({
        next: () => {
          this.snack.open('Trip deleted', 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: (err) => this.snack.open(err?.error?.message ?? 'Delete failed', 'Dismiss', { duration: 4000 })
      });
    });
  }
}
