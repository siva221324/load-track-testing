import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DealerPortalService } from '../../core/services/dealer-portal.service';
import { ReceiptService } from '../../core/services/receipt.service';
import { DealerStats } from '../../core/models/dealer-stats.model';
import { Payment, PaymentStatus } from '../../core/models/payment.model';

type StatusFilter = '' | PaymentStatus;

@Component({
  selector: 'app-dealer-dashboard',
  standalone: false,
  templateUrl: './dealer-dashboard.html',
  styleUrl: './dealer-dashboard.scss'
})
export class DealerDashboard implements OnInit {
  private portal = inject(DealerPortalService);
  private receipts = inject(ReceiptService);
  private snack = inject(MatSnackBar);

  loading = signal(true);
  stats = signal<DealerStats | null>(null);
  payments = signal<Payment[]>([]);
  statusFilter = signal<StatusFilter>('');
  overdueOnly = signal(false);

  paidPercent = computed(() => {
    const s = this.stats();
    if (!s || s.totalBilled === 0) return 0;
    return Math.min(100, Math.round((s.totalPaid / s.totalBilled) * 100));
  });

  displayedColumns = ['id', 'tripDate', 'truck', 'original', 'interest', 'final',
                      'paid', 'balance', 'dueDate', 'status', 'actions'];

  ngOnInit(): void {
    this.portal.myStats().subscribe({
      next: (s) => this.stats.set(s),
      error: (err) => this.snack.open(err?.error?.message ?? 'Failed to load stats', 'Dismiss', { duration: 4000 })
    });
    this.loadPayments();
  }

  loadPayments(): void {
    this.loading.set(true);
    this.portal.myPayments(this.statusFilter() || undefined, this.overdueOnly() || undefined, 0, 100).subscribe({
      next: (page) => { this.payments.set(page.content); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load payments', 'Dismiss', { duration: 4000 });
      }
    });
  }

  setStatusFilter(v: StatusFilter): void {
    this.statusFilter.set(v);
    this.loadPayments();
  }

  toggleOverdue(): void {
    this.overdueOnly.set(!this.overdueOnly());
    this.loadPayments();
  }

  downloadReceipt(payment: Payment): void {
    this.receipts.downloadForPayment(payment.id).subscribe({
      next: (blob) => {
        this.receipts.triggerBrowserDownload(blob, `REC-${payment.id}.pdf`);
        this.snack.open('Receipt downloaded', 'Dismiss', { duration: 2500 });
      },
      error: (err) => this.snack.open(err?.error?.message ?? 'Download failed', 'Dismiss', { duration: 4000 })
    });
  }
}
