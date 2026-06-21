import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { CreateLoginRequest, LoginInfo } from '../../core/models/login-info.model';
import { DriverService } from '../../core/services/driver.service';
import { DealerService } from '../../core/services/dealer.service';

export interface CreateLoginDialogData {
  entityType: 'driver' | 'dealer';
  entityId: number;
  entityName: string;
  existing?: LoginInfo | null;
}

@Component({
  selector: 'app-create-login-dialog',
  standalone: false,
  templateUrl: './create-login-dialog.html',
  styleUrl: './create-login-dialog.scss'
})
export class CreateLoginDialog {
  private fb = inject(FormBuilder);
  private drivers = inject(DriverService);
  private dealers = inject(DealerService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<CreateLoginDialog, LoginInfo | null>>(MatDialogRef);
  public data = inject<CreateLoginDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  hidePassword = signal(true);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const payload: CreateLoginRequest = this.form.getRawValue();

    const obs: Observable<LoginInfo> = this.data.entityType === 'driver'
      ? this.drivers.createLogin(this.data.entityId, payload)
      : this.dealers.createLogin(this.data.entityId, payload);

    obs.subscribe({
      next: (info) => {
        this.snack.open(
          `Login created: ${info.username} (${info.role})`,
          'Dismiss',
          { duration: 4000 }
        );
        this.dialogRef.close(info);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to create login', 'Dismiss', { duration: 5000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }
}
