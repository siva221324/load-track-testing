import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DealerService } from '../../core/services/dealer.service';
import { Dealer, DealerRequest } from '../../core/models/dealer.model';

export interface DealerFormDialogData {
  mode: 'add' | 'edit';
  dealer?: Dealer;
}

@Component({
  selector: 'app-dealer-form-dialog',
  standalone: false,
  templateUrl: './dealer-form-dialog.html',
  styleUrl: './dealer-form-dialog.scss'
})
export class DealerFormDialog {
  private fb = inject(FormBuilder);
  private dealers = inject(DealerService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<DealerFormDialog, Dealer | null>>(MatDialogRef);
  public data = inject<DealerFormDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  isEdit = this.data.mode === 'edit';

  form = this.fb.nonNullable.group({
    name:    [this.data.dealer?.name ?? '',    [Validators.required, Validators.maxLength(100)]],
    phone:   [this.data.dealer?.phone ?? '',   [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    address: [this.data.dealer?.address ?? '']
  });

  save(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    const payload = this.form.getRawValue() as DealerRequest;

    const obs = this.isEdit
      ? this.dealers.update(this.data.dealer!.id, payload)
      : this.dealers.create(payload);

    obs.subscribe({
      next: (dealer) => {
        this.snack.open(this.isEdit ? 'Dealer updated' : 'Dealer added', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(dealer);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Save failed', 'Dismiss', { duration: 4000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
