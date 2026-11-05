import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from './auth.service';

describe('adminGuard', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  const urlLogin = {} as UrlTree;

  const ejecutar = () =>
    TestBed.runInInjectionContext(() => adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated', 'hasRole']);
    router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue(urlLogin);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('permite el acceso a un administrador autenticado', () => {
    auth.isAuthenticated.and.returnValue(true);
    auth.hasRole.and.returnValue(true);

    expect(ejecutar()).toBeTrue();
    expect(auth.hasRole).toHaveBeenCalledWith('ROLE_ADMIN');
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('redirige a /login si el usuario no está autenticado', () => {
    auth.isAuthenticated.and.returnValue(false);
    auth.hasRole.and.returnValue(true);

    expect(ejecutar()).toBe(urlLogin);
    expect(router.createUrlTree).toHaveBeenCalledOnceWith(['/login']);
  });

  it('redirige a /login si el usuario autenticado no es administrador', () => {
    auth.isAuthenticated.and.returnValue(true);
    auth.hasRole.and.returnValue(false);

    expect(ejecutar()).toBe(urlLogin);
  });
});
