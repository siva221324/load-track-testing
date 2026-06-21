import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TripRequestService } from '../../core/services/trip-request.service';
import { TripRequest } from '../../core/models/trip-request.model';

export interface RejectDialogData {
  request: TripRequest;
}

@Component({
  selector: 'app-reject-dialog',
  standalone: false,
  templateUrl: './reject-dialog.html',
  styleUrl: './reject-dialog.scss'
})
export class RejectDialog {
  private fb = inject(FormBuilder);
  private requests = inject(TripRequestService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<RejectDialog, TripRequest | null>>(MatDialogRef);
  public data = inject<RejectDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);

  form = this.fb.nonNullable.group({
    reason: ['', [Validators.required, Validators.maxLength(2000)]]
  });

  reject(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.requests.reject(this.data.request.id, { reason: this.form.controls.reason.value }).subscribe({
      next: (updated) => {
        this.snack.open('Request rejected. Dealer has been notified.', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(updated);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Reject failed', 'Dismiss', { duration: 4000 });
      }
    });
  }

  cancel(): void { this.dialogRef.close(null); }
}
