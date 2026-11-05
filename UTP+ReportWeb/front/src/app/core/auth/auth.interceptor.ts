import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('auth_token');
  // Los registros de /api/auth requieren sesión de SUPERADMIN; solo el login va sin token.
  if (!token || request.url.endsWith('/api/auth/login')) return next(request);
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
