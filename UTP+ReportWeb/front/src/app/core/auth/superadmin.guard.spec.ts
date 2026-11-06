import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { superadminGuard } from './superadmin.guard';

describe('superadminGuard', () => {
  let autenticado: boolean;
  let roles: string[];

  const evaluar = () =>
    TestBed.runInInjectionContext(() => superadminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  beforeEach(() => {
    autenticado = true;
    roles = ['ROLE_SUPERADMIN'];
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: () => autenticado, hasRole: (rol: string) => roles.includes(rol) } },
      ],
    });
  });

  it('permite al superadministrador con sesión vigente', () => {
    expect(evaluar()).toBeTrue();
  });

  it('envía a /login sin sesión vigente', () => {
    autenticado = false;
    expect(String(evaluar())).toBe('/login');
  });

  it('envía a /login a un administrador', () => {
    roles = ['ROLE_ADMIN'];
    expect(String(evaluar())).toBe('/login');
  });
});
