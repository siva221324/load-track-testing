import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TripRequestService } from '../../core/services/trip-request.service';
import { TruckService } from '../../core/services/truck.service';
import { DriverService } from '../../core/services/driver.service';
import { TripRequest, ApproveTripRequestPayload } from '../../core/models/trip-request.model';
import { Truck } from '../../core/models/truck.model';
import { Driver } from '../../core/models/driver.model';

export interface ApproveDialogData {
  request: TripRequest;
}

@Component({
  selector: 'app-approve-dialog',
  standalone: false,
  templateUrl: './approve-dialog.html',
  styleUrl: './approve-dialog.scss'
})
export class ApproveDialog implements OnInit {
  private fb = inject(FormBuilder);
  private trucksApi = inject(TruckService);
  private driversApi = inject(DriverService);
  private requests = inject(TripRequestService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<ApproveDialog, TripRequest | null>>(MatDialogRef);
  public data = inject<ApproveDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  loading = signal(true);
  trucks = signal<Truck[]>([]);
  drivers = signal<Driver[]>([]);

  form = this.fb.nonNullable.group({
    truckId: this.fb.control<number | null>(null, Validators.required),
    driverId: this.fb.control<number | null>(null, Validators.required),
    tripDate: [this.data.request.requestedDate],
    adminNotes: ['']
  });

  ngOnInit(): void {
    // AVAILABLE trucks only — can't approve onto a busy truck
    this.trucksApi.list({ size: 200, sort: 'truckNumber,asc', status: 'AVAILABLE' }).subscribe({
      next: (page) => this.trucks.set(page.content)
    });
    this.driversApi.list({ size: 200, sort: 'name,asc' }).subscribe({
      next: (page) => { this.drivers.set(page.content); this.loading.set(false); }
    });
  }

  approve(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const v = this.form.getRawValue();
    const payload: ApproveTripRequestPayload = {
      truckId: v.truckId!,
      driverId: v.driverId!,
      tripDate: v.tripDate || null,
      adminNotes: v.adminNotes || undefined
    };
    this.requests.approve(this.data.request.id, payload).subscribe({
      next: (updated) => {
        this.snack.open(`Approved. Trip #${updated.approvedTripId} created.`, 'Dismiss', { duration: 4000 });
        this.dialogRef.close(updated);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Approval failed', 'Dismiss', { duration: 5000 });
      }
    });
  }

  cancel(): void { this.dialogRef.close(null); }
}
