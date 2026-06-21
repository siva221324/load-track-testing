import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DriverService } from '../../core/services/driver.service';
import { TruckService } from '../../core/services/truck.service';
import { Driver, DriverRequest } from '../../core/models/driver.model';
import { Truck } from '../../core/models/truck.model';

export interface DriverFormDialogData {
  mode: 'add' | 'edit';
  driver?: Driver;
}

@Component({
  selector: 'app-driver-form-dialog',
  standalone: false,
  templateUrl: './driver-form-dialog.html',
  styleUrl: './driver-form-dialog.scss'
})
export class DriverFormDialog implements OnInit {
  private fb = inject(FormBuilder);
  private drivers = inject(DriverService);
  private trucksApi = inject(TruckService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<DriverFormDialog, Driver | null>>(MatDialogRef);
  public data = inject<DriverFormDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  trucks = signal<Truck[]>([]);
  isEdit = this.data.mode === 'edit';

  form = this.fb.nonNullable.group({
    name:            [this.data.driver?.name ?? '',          [Validators.required, Validators.maxLength(100)]],
    phone:           [this.data.driver?.phone ?? '',         [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    licenseNumber:   [this.data.driver?.licenseNumber ?? '', [Validators.required, Validators.maxLength(30)]],
    address:         [this.data.driver?.address ?? ''],
    salaryPerTrip:   [this.data.driver?.salaryPerTrip ?? 0,  [Validators.required, Validators.min(0)]],
    assignedTruckId: this.fb.control<number | null>(this.data.driver?.assignedTruck?.id ?? null)
  });

  ngOnInit(): void {
    this.trucksApi.list({ size: 200, sort: 'truckNumber,asc' }).subscribe({
      next: (page) => this.trucks.set(page.content),
      error: () => this.snack.open('Failed to load trucks', 'Dismiss', { duration: 3000 })
    });
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    const payload = this.form.getRawValue() as DriverRequest;

    const obs = this.isEdit
      ? this.drivers.update(this.data.driver!.id, payload)
      : this.drivers.create(payload);

    obs.subscribe({
      next: (driver) => {
        this.snack.open(this.isEdit ? 'Driver updated' : 'Driver added', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(driver);
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
