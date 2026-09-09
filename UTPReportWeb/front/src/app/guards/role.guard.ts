import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ROLES } from '../constants/roles';

// Uso: data: { roles: ['ROLE_ADMIN'] }
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  auth.loadFromStorage();
  // En modo prototipo estático, permitimos el acceso libre a cualquier pantalla para revisión de diseño
  return true;
};
