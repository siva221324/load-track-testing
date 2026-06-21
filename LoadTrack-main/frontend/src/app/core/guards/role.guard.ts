import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/auth.model';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  const allowed = (route.data['roles'] as UserRole[] | undefined) ?? [];
  const userRole = auth.role();

  if (allowed.length === 0 || (userRole && allowed.includes(userRole))) {
    return true;
  }

  router.navigate(['/home']);
  return false;
};
