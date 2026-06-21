import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TripRequestService } from '../../core/services/trip-request.service';
import { TripRequest, TripRequestStatus } from '../../core/models/trip-request.model';
import { ApproveDialog } from './approve-dialog';
import { RejectDialog } from './reject-dialog';

type StatusFilter = '' | TripRequestStatus;

@Component({
  selector: 'app-admin-requests',
  standalone: false,
  templateUrl: './admin-requests.html',
  styleUrl: './admin-requests.scss'
})
export class AdminRequests implements OnInit {
  private requests = inject(TripRequestService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  loading = signal(false);
  data = signal<TripRequest[]>([]);
  statusFilter = signal<StatusFilter>('PENDING');
  pendingCount = signal(0);

  displayedColumns = ['id', 'createdAt', 'dealer', 'sandType', 'tons', 'estimatedAmount',
                      'requestedDate', 'route', 'status', 'actions'];

  ngOnInit(): void {
    this.load();
    this.refreshPendingCount();
  }

  load(): void {
    this.loading.set(true);
    this.requests.listForAdmin(this.statusFilter() || undefined, 0, 100).subscribe({
      next: (page) => { this.data.set(page.content); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load requests', 'Dismiss', { duration: 4000 });
      }
    });
  }

  refreshPendingCount(): void {
    this.requests.pendingCount().subscribe({
      next: (res) => this.pendingCount.set(res.count)
    });
  }

  setStatusFilter(v: StatusFilter): void {
    this.statusFilter.set(v);
    this.load();
  }

  approve(request: TripRequest): void {
    if (request.status !== 'PENDING') return;
    this.dialog.open(ApproveDialog, { data: { request }, width: '640px' })
      .afterClosed().subscribe((r) => {
        if (r) { this.load(); this.refreshPendingCount(); }
      });
  }

  reject(request: TripRequest): void {
    if (request.status !== 'PENDING') return;
    this.dialog.open(RejectDialog, { data: { request }, width: '560px' })
      .afterClosed().subscribe((r) => {
        if (r) { this.load(); this.refreshPendingCount(); }
      });
  }
}
