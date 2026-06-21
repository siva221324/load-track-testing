import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.html',
  styleUrl: './login.scss'
})
export class Signup {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private snack = inject(MatSnackBar);

  loading = signal(false);
  hidePassword = signal(true);
  hideConfirm = signal(true);
  passwordValue = signal('');

  /** Strength score: 0 (none) → 4 (strong). */
  readonly strengthScore = computed(() => this.computeStrength(this.passwordValue()));
  readonly strengthLabel = computed(() => {
    const s = this.strengthScore();
    if (s === 0) return '';
    if (s <= 2) return 'Weak';
    if (s === 3) return 'Medium';
    return 'Strong';
  });
  readonly strengthClass = computed(() => {
    const s = this.strengthScore();
    if (s === 0) return '';
    if (s <= 2) return 'weak';
    if (s === 3) return 'medium';
    return 'strong';
  });

  form = this.fb.nonNullable.group({
    username: ['', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(50),
      Validators.pattern(/^[a-zA-Z0-9_.-]+$/)
    ]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordsMatch });

  constructor() {
    this.form.controls.password.valueChanges.subscribe((v) => this.passwordValue.set(v ?? ''));
  }

  private passwordsMatch(group: AbstractControl): ValidationErrors | null {
    const pw = group.get('password')?.value;
    const cf = group.get('confirmPassword')?.value;
    return pw && cf && pw !== cf ? { passwordMismatch: true } : null;
  }

  private computeStrength(pw: string): number {
    if (!pw) return 0;
    let score = 0;
    if (pw.length >= 6) score++;
    if (pw.length >= 10) score++;
    if (/[A-Z]/.test(pw) && /[a-z]/.test(pw)) score++;
    if (/\d/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    return Math.min(4, score);
  }

  segmentClass(i: number): string {
    const s = this.strengthScore();
    if (i >= s) return '';
    return 'active-' + this.strengthClass();
  }

  submit(): void {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    const { username, password } = this.form.getRawValue();
    this.auth.signup(username, password).subscribe({
      next: () => {
        this.snack.open('Account created. Please log in.', 'Dismiss', { duration: 4000 });
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Signup failed. Try a different username.', 'Dismiss', { duration: 5000 });
      }
    });
  }
}
