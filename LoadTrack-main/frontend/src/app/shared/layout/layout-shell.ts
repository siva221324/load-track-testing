import { Component, computed, inject, signal } from '@angular/core';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';

interface NavLink {
  label: string;
  icon: string;
  route: string;
  roles: ('ADMIN' | 'DRIVER' | 'DEALER')[];
}

@Component({
  selector: 'app-layout-shell',
  standalone: false,
  templateUrl: './layout-shell.html',
  styleUrl: './layout-shell.scss'
})
export class LayoutShell {
  protected auth = inject(AuthService);
  private breakpoints = inject(BreakpointObserver);

  readonly isHandset = signal(false);

  constructor() {
    this.breakpoints
      .observe([Breakpoints.Handset, Breakpoints.TabletPortrait])
      .pipe(takeUntilDestroyed())
      .subscribe(r => this.isHandset.set(r.matches));
  }

  private readonly allLinks: NavLink[] = [
    { label: 'Dashboard',     icon: 'dashboard',      route: '/app/home',    roles: ['ADMIN'] },
    { label: 'Trucks',        icon: 'local_shipping', route: '/app/trucks',  roles: ['ADMIN'] },
    { label: 'Drivers',       icon: 'badge',          route: '/app/drivers', roles: ['ADMIN'] },
    { label: 'Dealers',       icon: 'storefront',     route: '/app/dealers',    roles: ['ADMIN'] },
    { label: 'Sand Types',    icon: 'category',       route: '/app/sand-types', roles: ['ADMIN'] },
    { label: 'Trips',         icon: 'route',          route: '/app/trips',      roles: ['ADMIN'] },
    { label: 'Payments',      icon: 'payments',       route: '/app/payments',   roles: ['ADMIN'] },
    { label: 'Reports',       icon: 'assessment',     route: '/app/reports',    roles: ['ADMIN'] },
    { label: 'Settings',      icon: 'settings',       route: '/app/settings',   roles: ['ADMIN'] },
    { label: 'My Dashboard',  icon: 'dashboard',      route: '/app/driver',     roles: ['DRIVER'] },
    { label: 'My Account',    icon: 'account_balance_wallet', route: '/app/dealer', roles: ['DEALER'] },
    { label: 'Request Trips', icon: 'send',           route: '/app/dealer-requests', roles: ['DEALER'] },
    { label: 'Trip Requests', icon: 'inbox',          route: '/app/trip-requests',   roles: ['ADMIN'] },
    { label: 'Account',       icon: 'manage_accounts', route: '/app/account',   roles: ['ADMIN', 'DRIVER', 'DEALER'] }
  ];

  readonly visibleLinks = computed<NavLink[]>(() => {
    const role = this.auth.role();
    if (!role) return [];
    return this.allLinks.filter(l => l.roles.includes(role));
  });

  logout(): void {
    this.auth.logout();
  }
}
