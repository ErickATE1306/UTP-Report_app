import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/** ROLE_ADMIN se admite porque select-rol permite al administrador actuar como usuario. */
export const usuarioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const permitido = auth.isAuthenticated() && (auth.hasRole('ROLE_USUARIO') || auth.hasRole('ROLE_ADMIN'));
  return permitido ? true : inject(Router).createUrlTree(['/login']);
};
