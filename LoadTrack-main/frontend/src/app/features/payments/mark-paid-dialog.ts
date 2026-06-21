import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PaymentService } from '../../core/services/payment.service';
import { Payment } from '../../core/models/payment.model';

export interface MarkPaidDialogData {
  payment: Payment;
}

@Component({
  selector: 'app-mark-paid-dialog',
  standalone: false,
  templateUrl: './mark-paid-dialog.html',
  styleUrl: './mark-paid-dialog.scss'
})
export class MarkPaidDialog {
  private fb = inject(FormBuilder);
  private payments = inject(PaymentService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<MarkPaidDialog, Payment | null>>(MatDialogRef);
  public data = inject<MarkPaidDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  payment = this.data.payment;

  form = this.fb.nonNullable.group({
    paidAmount: [this.data.payment.balanceDue, [Validators.required, Validators.min(0.01)]]
  });

  payFull(): void {
    this.form.controls.paidAmount.setValue(this.payment.balanceDue);
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.payments.markPaid(this.payment.id, { paidAmount: Number(this.form.controls.paidAmount.value) })
      .subscribe({
        next: (updated) => {
          this.snack.open(
            updated.paymentStatus === 'PAID' ? 'Payment fully paid' : 'Partial payment recorded',
            'Dismiss',
            { duration: 3000 }
          );
          this.dialogRef.close(updated);
        },
        error: (err) => {
          this.saving.set(false);
          this.snack.open(err?.error?.message ?? 'Payment failed', 'Dismiss', { duration: 5000 });
        }
      });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
