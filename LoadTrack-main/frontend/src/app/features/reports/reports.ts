import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReportService } from '../../core/services/report.service';
import { DriverService } from '../../core/services/driver.service';
import { TruckService } from '../../core/services/truck.service';
import { DealerService } from '../../core/services/dealer.service';
import {
  ExportFormat,
  PaymentReportRow,
  TripReportRow
} from '../../core/models/report.model';
import { Driver } from '../../core/models/driver.model';
import { Truck } from '../../core/models/truck.model';
import { Dealer } from '../../core/models/dealer.model';

type Tab = 'trips' | 'payments';

@Component({
  selector: 'app-reports',
  standalone: false,
  templateUrl: './reports.html',
  styleUrl: './reports.scss'
})
export class Reports implements OnInit {
  private fb = inject(FormBuilder);
  private reports = inject(ReportService);
  private trucksApi = inject(TruckService);
  private driversApi = inject(DriverService);
  private dealersApi = inject(DealerService);
  private snack = inject(MatSnackBar);

  activeTab = signal<Tab>('trips');
  loading = signal(false);
  exporting = signal(false);

  trucks = signal<Truck[]>([]);
  drivers = signal<Driver[]>([]);
  dealers = signal<Dealer[]>([]);

  tripRows = signal<TripReportRow[]>([]);
  paymentRows = signal<PaymentReportRow[]>([]);

  tripDisplayedColumns = ['tripId', 'tripDate', 'truckNumber', 'driverName', 'dealerName',
                          'sandTypeName', 'tons', 'totalAmount', 'status'];
  paymentDisplayedColumns = ['paymentId', 'tripDate', 'truckNumber', 'dealerName',
                              'originalAmount', 'interestAmount', 'finalAmount',
                              'paidAmount', 'balanceDue', 'paymentStatus', 'overdue'];

  tripFilters = this.fb.nonNullable.group({
    from: [''],
    to: [''],
    truckId: this.fb.control<number | null>(null),
    driverId: this.fb.control<number | null>(null),
    dealerId: this.fb.control<number | null>(null),
    status: ['']
  });

  paymentFilters = this.fb.nonNullable.group({
    from: [''],
    to: [''],
    dealerId: this.fb.control<number | null>(null),
    paymentStatus: [''],
    overdueOnly: [false]
  });

  ngOnInit(): void {
    this.trucksApi.list({ size: 200, sort: 'truckNumber,asc' }).subscribe(p => this.trucks.set(p.content));
    this.driversApi.list({ size: 200, sort: 'name,asc' }).subscribe(p => this.drivers.set(p.content));
    this.dealersApi.list({ size: 200, sort: 'name,asc' }).subscribe(p => this.dealers.set(p.content));
    this.runTrips();
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
    if (tab === 'trips' && this.tripRows().length === 0) this.runTrips();
    if (tab === 'payments' && this.paymentRows().length === 0) this.runPayments();
  }

  runTrips(): void {
    this.loading.set(true);
    const f = this.tripFilters.getRawValue();
    this.reports.trips({
      from: f.from || undefined,
      to: f.to || undefined,
      truckId: f.truckId ?? undefined,
      driverId: f.driverId ?? undefined,
      dealerId: f.dealerId ?? undefined,
      status: f.status || undefined
    }).subscribe({
      next: (rows) => { this.tripRows.set(rows); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load trips report', 'Dismiss', { duration: 4000 });
      }
    });
  }

  runPayments(): void {
    this.loading.set(true);
    const f = this.paymentFilters.getRawValue();
    this.reports.payments({
      from: f.from || undefined,
      to: f.to || undefined,
      dealerId: f.dealerId ?? undefined,
      paymentStatus: f.paymentStatus || undefined,
      overdueOnly: f.overdueOnly || undefined
    }).subscribe({
      next: (rows) => { this.paymentRows.set(rows); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load payments report', 'Dismiss', { duration: 4000 });
      }
    });
  }

  exportCurrent(format: ExportFormat): void {
    this.exporting.set(true);
    const ext = format === 'pdf' ? 'pdf' : 'xlsx';
    if (this.activeTab() === 'trips') {
      const f = this.tripFilters.getRawValue();
      this.reports.exportTrips(format, {
        from: f.from || undefined,
        to: f.to || undefined,
        truckId: f.truckId ?? undefined,
        driverId: f.driverId ?? undefined,
        dealerId: f.dealerId ?? undefined,
        status: f.status || undefined
      }).subscribe({
        next: (blob) => {
          this.reports.triggerDownload(blob, `trips-report.${ext}`);
          this.exporting.set(false);
          this.snack.open(`Trips report (${ext.toUpperCase()}) downloaded`, 'Dismiss', { duration: 2500 });
        },
        error: () => { this.exporting.set(false); this.snack.open('Export failed', 'Dismiss', { duration: 4000 }); }
      });
    } else {
      const f = this.paymentFilters.getRawValue();
      this.reports.exportPayments(format, {
        from: f.from || undefined,
        to: f.to || undefined,
        dealerId: f.dealerId ?? undefined,
        paymentStatus: f.paymentStatus || undefined,
        overdueOnly: f.overdueOnly || undefined
      }).subscribe({
        next: (blob) => {
          this.reports.triggerDownload(blob, `payments-report.${ext}`);
          this.exporting.set(false);
          this.snack.open(`Payments report (${ext.toUpperCase()}) downloaded`, 'Dismiss', { duration: 2500 });
        },
        error: () => { this.exporting.set(false); this.snack.open('Export failed', 'Dismiss', { duration: 4000 }); }
      });
    }
  }
}
