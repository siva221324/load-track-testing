import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.html',
  styleUrl: './login.scss'
})
export class ForgotPassword {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  loading = signal(false);
  resetTempPassword = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.auth.forgotPassword(this.form.controls.username.value).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.resetTempPassword.set(res.temporaryPassword);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(
          err?.error?.message ?? 'Reset failed. Check the username and try again.',
          'Dismiss',
          { duration: 5000 }
        );
      }
    });
  }
}
