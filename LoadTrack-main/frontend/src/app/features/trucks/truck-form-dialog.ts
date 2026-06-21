import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TruckService } from '../../core/services/truck.service';
import { Truck, TruckRequest } from '../../core/models/truck.model';

export interface TruckFormDialogData {
  mode: 'add' | 'edit';
  truck?: Truck;
}

@Component({
  selector: 'app-truck-form-dialog',
  standalone: false,
  templateUrl: './truck-form-dialog.html',
  styleUrl: './truck-form-dialog.scss'
})
export class TruckFormDialog {
  private fb = inject(FormBuilder);
  private trucks = inject(TruckService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<TruckFormDialog, Truck | null>>(MatDialogRef);
  public data = inject<TruckFormDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  isEdit = this.data.mode === 'edit';

  statuses = ['AVAILABLE', 'ON_TRIP', 'MAINTENANCE'];

  form = this.fb.nonNullable.group({
    truckNumber:     [this.data.truck?.truckNumber ?? '',     [Validators.required, Validators.maxLength(20)]],
    model:           [this.data.truck?.model ?? '',           [Validators.required, Validators.maxLength(100)]],
    capacityTons:    [this.data.truck?.capacityTons ?? 0,     [Validators.required, Validators.min(0.01)]],
    insuranceNumber: [this.data.truck?.insuranceNumber ?? ''],
    rcNumber:        [this.data.truck?.rcNumber ?? ''],
    status:          [this.data.truck?.status ?? 'AVAILABLE']
  });

  save(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    const payload = this.form.getRawValue() as TruckRequest;

    const obs = this.isEdit
      ? this.trucks.update(this.data.truck!.id, payload)
      : this.trucks.create(payload);

    obs.subscribe({
      next: (truck) => {
        this.snack.open(this.isEdit ? 'Truck updated' : 'Truck added', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(truck);
      },
      error: (err) => {
        this.saving.set(false);
        const msg = err?.error?.message ?? 'Save failed';
        this.snack.open(msg, 'Dismiss', { duration: 4000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
