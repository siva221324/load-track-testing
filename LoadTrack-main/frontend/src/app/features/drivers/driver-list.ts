import { Component, OnInit, inject, signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DriverService } from '../../core/services/driver.service';
import { Driver } from '../../core/models/driver.model';
import { DriverFormDialog } from './driver-form-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';
import { CreateLoginDialog, CreateLoginDialogData } from '../../shared/dialogs/create-login-dialog';

@Component({
  selector: 'app-driver-list',
  standalone: false,
  templateUrl: './driver-list.html',
  styleUrl: './driver-list.scss'
})
export class DriverList implements OnInit {
  private drivers = inject(DriverService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'name', 'phone', 'licenseNumber', 'salaryPerTrip', 'assignedTruck', 'actions'];

  data = signal<Driver[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  searchTerm = signal('');

  pageSize = 10;
  pageIndex = 0;
  sort = 'id,asc';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.drivers.list({
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.sort,
      search: this.searchTerm() || undefined
    }).subscribe({
      next: (page) => {
        this.data.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load drivers', 'Dismiss', { duration: 4000 });
      }
    });
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.load(); }
  onSort(s: Sort): void { this.sort = s.direction ? `${s.active},${s.direction}` : 'id,asc'; this.load(); }
  applySearch(value: string): void { this.searchTerm.set(value.trim()); this.pageIndex = 0; this.load(); }

  add(): void {
    const ref = this.dialog.open(DriverFormDialog, { data: { mode: 'add' } });
    ref.afterClosed().subscribe((created: Driver | null) => {
      if (!created) return;
      this.load();
      if (created.loginInfo) {
        this.snack.open(
          `Login auto-created — Username: ${created.loginInfo.username} · Password: Loadtrack@123 (share with the driver)`,
          'Got it',
          { duration: 12000, panelClass: 'login-snack' }
        );
      }
    });
  }

  edit(driver: Driver): void {
    const ref = this.dialog.open(DriverFormDialog, { data: { mode: 'edit', driver } });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  createLogin(driver: Driver): void {
    this.drivers.getLogin(driver.id).subscribe((existing) => {
      const data: CreateLoginDialogData = {
        entityType: 'driver',
        entityId: driver.id,
        entityName: driver.name,
        existing
      };
      this.dialog.open(CreateLoginDialog, { data });
    });
  }

  remove(driver: Driver): void {
    const data: ConfirmDialogData = {
      title: 'Delete Driver',
      message: `Permanently delete driver ${driver.name}? This cannot be undone.`,
      confirmText: 'Delete',
      danger: true
    };
    const ref = this.dialog.open(ConfirmDialog, { data });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.drivers.delete(driver.id).subscribe({
        next: () => {
          this.snack.open('Driver deleted', 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: (err) => this.snack.open(err?.error?.message ?? 'Delete failed', 'Dismiss', { duration: 4000 })
      });
    });
  }
}
