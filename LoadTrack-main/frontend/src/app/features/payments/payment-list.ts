import { Component, OnInit, inject, signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PaymentService } from '../../core/services/payment.service';
import { ReceiptService } from '../../core/services/receipt.service';
import { Payment, PaymentStatus } from '../../core/models/payment.model';
import { MarkPaidDialog } from './mark-paid-dialog';

type StatusFilter = '' | PaymentStatus;

@Component({
  selector: 'app-payment-list',
  standalone: false,
  templateUrl: './payment-list.html',
  styleUrl: './payment-list.scss'
})
export class PaymentList implements OnInit {
  private payments = inject(PaymentService);
  private receipts = inject(ReceiptService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'tripDate', 'truck', 'dealer', 'original', 'interest',
                      'final', 'paid', 'balance', 'status', 'actions'];

  data = signal<Payment[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  statusFilter = signal<StatusFilter>('');
  overdueOnly = signal(false);

  pageSize = 10;
  pageIndex = 0;
  sort = 'id,desc';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.payments.list({
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.sort,
      status: this.statusFilter() || undefined,
      overdueOnly: this.overdueOnly() || undefined
    }).subscribe({
      next: (page) => {
        this.data.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load payments', 'Dismiss', { duration: 4000 });
      }
    });
  }

  setStatusFilter(v: StatusFilter): void {
    this.statusFilter.set(v);
    this.pageIndex = 0;
    this.load();
  }

  toggleOverdue(): void {
    this.overdueOnly.set(!this.overdueOnly());
    this.pageIndex = 0;
    this.load();
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.load(); }
  onSort(s: Sort): void { this.sort = s.direction ? `${s.active},${s.direction}` : 'id,desc'; this.load(); }

  pay(payment: Payment): void {
    if (payment.paymentStatus === 'PAID') {
      this.snack.open('This payment is already fully paid', 'Dismiss', { duration: 3000 });
      return;
    }
    const ref = this.dialog.open(MarkPaidDialog, { data: { payment }, width: '520px' });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  downloadReceipt(payment: Payment): void {
    this.receipts.downloadForPayment(payment.id).subscribe({
      next: (blob) => {
        this.receipts.triggerBrowserDownload(blob, `REC-${payment.id}.pdf`);
        this.snack.open('Receipt downloaded', 'Dismiss', { duration: 2500 });
      },
      error: (err) => this.snack.open(
        err?.error?.message ?? 'Failed to download receipt',
        'Dismiss',
        { duration: 4000 }
      )
    });
  }
}
