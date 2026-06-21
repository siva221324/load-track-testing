import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';

type LoginRole = 'ADMIN' | 'DRIVER' | 'DEALER';

interface RoleProfile {
  label: string;
  icon: string;
  helper: string;
  accent: string;
}

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snack = inject(MatSnackBar);

  loading = signal(false);
  hidePassword = signal(true);
  selectedRole = signal<LoginRole>('ADMIN');

  private profiles: Record<LoginRole, RoleProfile> = {
    ADMIN: {
      label: 'Admin',
      icon: 'admin_panel_settings',
      helper: 'Full access to manage trucks, drivers, dealers, trips, and payments.',
      accent: '#1976d2'
    },
    DRIVER: {
      label: 'Driver',
      icon: 'badge',
      helper: 'View your assigned trips and earnings.',
      accent: '#2e7d32'
    },
    DEALER: {
      label: 'Dealer',
      icon: 'storefront',
      helper: 'Track your payments, dues, and download receipts.',
      accent: '#e65100'
    }
  };

  profile = computed<RoleProfile>(() => this.profiles[this.selectedRole()]);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  selectRole(role: LoginRole): void {
    this.selectedRole.set(role);
  }

  submit(): void {
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        const defaultRoute = res.role === 'ADMIN'  ? '/app/home'
                           : res.role === 'DRIVER' ? '/app/driver'
                           :                          '/app/dealer';
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? defaultRoute;
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = err?.error?.message ?? 'Login failed. Please try again.';
        this.snack.open(msg, 'Dismiss', { duration: 4000 });
      }
    });
  }
}
