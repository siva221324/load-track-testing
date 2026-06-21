import { Component, OnInit, inject, signal } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DealerService } from '../../core/services/dealer.service';
import { Dealer } from '../../core/models/dealer.model';
import { DealerFormDialog } from './dealer-form-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';
import { CreateLoginDialog, CreateLoginDialogData } from '../../shared/dialogs/create-login-dialog';

@Component({
  selector: 'app-dealer-list',
  standalone: false,
  templateUrl: './dealer-list.html',
  styleUrl: './dealer-list.scss'
})
export class DealerList implements OnInit {
  private dealers = inject(DealerService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'name', 'phone', 'address', 'actions'];

  data = signal<Dealer[]>([]);
  totalElements = signal(0);
  loading = signal(false);
  searchTerm = signal('');

  pageSize = 10;
  pageIndex = 0;
  sort = 'id,asc';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.dealers.list({
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
        this.snack.open(err?.error?.message ?? 'Failed to load dealers', 'Dismiss', { duration: 4000 });
      }
    });
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.load(); }
  onSort(s: Sort): void { this.sort = s.direction ? `${s.active},${s.direction}` : 'id,asc'; this.load(); }
  applySearch(value: string): void { this.searchTerm.set(value.trim()); this.pageIndex = 0; this.load(); }

  add(): void {
    const ref = this.dialog.open(DealerFormDialog, { data: { mode: 'add' } });
    ref.afterClosed().subscribe((created: Dealer | null) => {
      if (!created) return;
      this.load();
      if (created.loginInfo) {
        this.snack.open(
          `Login auto-created — Username: ${created.loginInfo.username} · Password: Loadtrack@123 (share with the dealer)`,
          'Got it',
          { duration: 12000, panelClass: 'login-snack' }
        );
      }
    });
  }

  edit(dealer: Dealer): void {
    const ref = this.dialog.open(DealerFormDialog, { data: { mode: 'edit', dealer } });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  createLogin(dealer: Dealer): void {
    this.dealers.getLogin(dealer.id).subscribe((existing) => {
      const data: CreateLoginDialogData = {
        entityType: 'dealer',
        entityId: dealer.id,
        entityName: dealer.name,
        existing
      };
      this.dialog.open(CreateLoginDialog, { data });
    });
  }

  remove(dealer: Dealer): void {
    const data: ConfirmDialogData = {
      title: 'Delete Dealer',
      message: `Permanently delete dealer ${dealer.name}? This cannot be undone.`,
      confirmText: 'Delete',
      danger: true
    };
    const ref = this.dialog.open(ConfirmDialog, { data });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.dealers.delete(dealer.id).subscribe({
        next: () => {
          this.snack.open('Dealer deleted', 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: (err) => this.snack.open(err?.error?.message ?? 'Delete failed', 'Dismiss', { duration: 4000 })
      });
    });
  }
}
