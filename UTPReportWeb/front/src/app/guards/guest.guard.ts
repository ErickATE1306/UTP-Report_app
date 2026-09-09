import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ROLES } from '../constants/roles';

// Evita que usuarios autenticados ingresen a rutas de invitado (ej: /login)
export const guestGuard: CanActivateFn = () => {
  // En modo prototipo estático, permitimos ver la pantalla de login en cualquier momento
  return true;
};
