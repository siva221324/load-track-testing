import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccountService, AccountInfo } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-account-settings',
  standalone: false,
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.scss'
})
export class AccountSettings implements OnInit {
  private fb = inject(FormBuilder);
  private accountApi = inject(AccountService);
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);

  loading = signal(true);
  saving = signal(false);
  account = signal<AccountInfo | null>(null);

  hideCurrent = signal(true);
  hideNew = signal(true);
  hideConfirm = signal(true);

  form = this.fb.nonNullable.group({
    currentPassword: ['', [Validators.required]],
    newPassword:     ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordsMatch });

  private passwordsMatch(group: AbstractControl): ValidationErrors | null {
    const newPw = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return newPw && confirm && newPw !== confirm ? { passwordMismatch: true } : null;
  }

  ngOnInit(): void {
    this.accountApi.me().subscribe({
      next: (info) => { this.account.set(info); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load account', 'Dismiss', { duration: 4000 });
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.accountApi.changePassword({
      currentPassword: v.currentPassword,
      newPassword: v.newPassword
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.form.reset();
        this.snack.open('Password changed. Please log in again.', 'Dismiss', { duration: 5000 });
        // Force re-login since the JWT was issued for the old creds
        setTimeout(() => this.auth.logout(), 1500);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to change password', 'Dismiss', { duration: 5000 });
      }
    });
  }
}
