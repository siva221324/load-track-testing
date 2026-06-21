import { Component, OnInit, inject, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SandTypeService } from '../../core/services/sand-type.service';
import { SandType } from '../../core/models/sand-type.model';
import { SandTypeFormDialog } from './sand-type-form-dialog';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';

@Component({
  selector: 'app-sand-type-list',
  standalone: false,
  templateUrl: './sand-type-list.html',
  styleUrl: './sand-type-list.scss'
})
export class SandTypeList implements OnInit {
  private sandTypes = inject(SandTypeService);
  private dialog = inject(MatDialog);
  private snack = inject(MatSnackBar);

  displayedColumns = ['id', 'name', 'pricePerTon', 'actions'];
  data = signal<SandType[]>([]);
  loading = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.sandTypes.list().subscribe({
      next: (rows) => { this.data.set(rows); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load', 'Dismiss', { duration: 4000 });
      }
    });
  }

  add(): void {
    const ref = this.dialog.open(SandTypeFormDialog, { data: { mode: 'add' } });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  edit(sandType: SandType): void {
    const ref = this.dialog.open(SandTypeFormDialog, { data: { mode: 'edit', sandType } });
    ref.afterClosed().subscribe((r) => { if (r) this.load(); });
  }

  remove(sandType: SandType): void {
    const data: ConfirmDialogData = {
      title: 'Delete Sand Type',
      message: `Permanently delete "${sandType.name}"? This will fail if any trip uses this sand type.`,
      confirmText: 'Delete',
      danger: true
    };
    const ref = this.dialog.open(ConfirmDialog, { data });
    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.sandTypes.delete(sandType.id).subscribe({
        next: () => {
          this.snack.open('Sand type deleted', 'Dismiss', { duration: 3000 });
          this.load();
        },
        error: (err) => this.snack.open(err?.error?.message ?? 'Delete failed', 'Dismiss', { duration: 4000 })
      });
    });
  }
}
