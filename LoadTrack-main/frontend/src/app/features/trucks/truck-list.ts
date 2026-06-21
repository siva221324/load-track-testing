import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TruckService } from '../../core/services/truck.service';
import { Truck } from '../../core/models/truck.model';
import { TruckFormDialog } from './truck-form-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';

@Component({
  selector: 'app-truck-list',
  standalone: false,
  templateUrl: './truck-list.html',
  styleUrl: './truck-list.scss'
})
export class TruckList implements OnInit {
  private trucks = inject(TruckService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'truckNumber', 'model', 'capacityTons', 'status', 'actions'];

  data = signal<Truck[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  searchTerm = signal('');

  pageSize = 10;
  pageIndex = 0;
  sort = 'id,asc';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.trucks.list({
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
        this.snack.open(err?.error?.message ?? 'Failed to load trucks', 'Dismiss', { duration: 4000 });
      }
    });
  }

  onPage(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.load();
  }

  onSort(s: Sort): void {
    this.sort = s.direction ? `${s.active},${s.direction}` : 'id,asc';
    this.load();
  }

  applySearch(value: string): void {
    this.searchTerm.set(value.trim());
    this.pageIndex = 0;
    this.load();
  }

  add(): void {
    const ref = this.dialog.open(TruckFormDialog, { data: { mode: 'add' } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  edit(truck: Truck): void {
    const ref = this.dialog.open(TruckFormDialog, { data: { mode: 'edit', truck } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  remove(truck: Truck): void {
    const data: ConfirmDialogData = {
      title: 'Delete Truck',
      message: `Permanently delete truck ${truck.truckNumber}? This cannot be undone.`,
      confirmText: 'Delete',
      danger: true
    };
    const ref = this.dialog.open(ConfirmDialog, { data });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.trucks.delete(truck.id).subscribe({
        next: () => {
          this.snack.open('Truck deleted', 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: (err) => {
          this.snack.open(err?.error?.message ?? 'Delete failed', 'Dismiss', { duration: 4000 });
        }
      });
    });
  }
}
