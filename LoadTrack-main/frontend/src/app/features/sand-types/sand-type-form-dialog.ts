import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SandTypeService } from '../../core/services/sand-type.service';
import { SandType, SandTypeRequest } from '../../core/models/sand-type.model';

export interface SandTypeFormDialogData {
  mode: 'add' | 'edit';
  sandType?: SandType;
}

@Component({
  selector: 'app-sand-type-form-dialog',
  standalone: false,
  templateUrl: './sand-type-form-dialog.html',
  styleUrl: './sand-type-form-dialog.scss'
})
export class SandTypeFormDialog {
  private fb = inject(FormBuilder);
  private sandTypes = inject(SandTypeService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<SandTypeFormDialog, SandType | null>>(MatDialogRef);
  public data = inject<SandTypeFormDialogData>(MAT_DIALOG_DATA);

  saving = signal(false);
  isEdit = this.data.mode === 'edit';

  form = this.fb.nonNullable.group({
    name:         [this.data.sandType?.name ?? '',         [Validators.required, Validators.maxLength(50)]],
    pricePerTon:  [this.data.sandType?.pricePerTon ?? 0,   [Validators.required, Validators.min(0.01)]]
  });

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const payload = this.form.getRawValue() as SandTypeRequest;
    const obs = this.isEdit
      ? this.sandTypes.update(this.data.sandType!.id, payload)
      : this.sandTypes.create(payload);

    obs.subscribe({
      next: (s) => {
        this.snack.open(this.isEdit ? 'Sand type updated' : 'Sand type added', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(s);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Save failed', 'Dismiss', { duration: 4000 });
      }
    });
  }

  cancel(): void { this.dialogRef.close(null); }
}
