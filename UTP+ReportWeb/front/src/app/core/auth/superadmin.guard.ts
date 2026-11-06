import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const superadminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() && auth.hasRole('ROLE_SUPERADMIN')
    ? true
    : inject(Router).createUrlTree(['/login']);
};
