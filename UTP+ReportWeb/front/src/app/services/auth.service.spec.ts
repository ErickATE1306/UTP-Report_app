import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ROLES } from '../constants/roles';

function jwt(payload: object): string {
  const b64 = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `header.${b64}.firma`;
}

describe('services/AuthService', () => {
  const LOGIN_URL = 'http://localhost:8080/api/auth/login';
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  const limpiarClases = () => {
    for (const el of [document.documentElement, document.body]) el.classList.remove('dark-mode', 'superadmin-force-light');
  };

  function iniciarSesion(roles: string[], token = 'abc') {
    service.login('ana', 'secreto').subscribe();
    httpMock.expectOne(LOGIN_URL).flush({ token, tipoToken: 'Bearer', roles });
  }

  beforeEach(() => {
    localStorage.clear();
    limpiarClases();
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: Router, useValue: router }],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    service.logout();
    localStorage.clear();
    limpiarClases();
  });

  describe('login', () => {
    it('envía las credenciales y guarda token y roles', () => {
      service.login('ana', 'secreto').subscribe();

      const req = httpMock.expectOne(LOGIN_URL);
      expect(req.request.body).toEqual({ usernameOrCorreo: 'ana', password: 'secreto' });
      req.flush({ token: 'abc', tipoToken: 'Bearer', roles: [ROLES.ADMIN] });

      expect(service.getToken()).toBe('abc');
      expect(service.hasRole(ROLES.ADMIN)).toBeTrue();
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('marca como no autenticado y propaga el error si falla', () => {
      let huboError = false;
      service.login('ana', 'mala').subscribe({ error: () => (huboError = true) });

      httpMock.expectOne(LOGIN_URL).flush(null, { status: 401, statusText: 'Unauthorized' });

      expect(huboError).toBeTrue();
      expect(service.isAuthenticated()).toBeFalse();
      expect(service.getToken()).toBeNull();
    });

    it('fuerza el modo claro para un SUPERADMIN', () => {
      document.documentElement.classList.add('dark-mode');

      iniciarSesion([ROLES.SUPERADMIN]);

      expect(document.documentElement.classList.contains('dark-mode')).toBeFalse();
      expect(document.documentElement.classList.contains('superadmin-force-light')).toBeTrue();
    });

    it('no fuerza el modo claro para otros roles', () => {
      iniciarSesion([ROLES.USUARIO]);
      expect(document.documentElement.classList.contains('superadmin-force-light')).toBeFalse();
    });
  });

  describe('logout', () => {
    it('limpia la sesión y redirige a /login', () => {
      iniciarSesion([ROLES.ADMIN]);
      service.setAdminActingAsUser(true);
      service.setAdminRoleSelected(true);

      service.logout();

      expect(service.getToken()).toBeNull();
      expect(localStorage.getItem('auth_roles')).toBeNull();
      expect(service.isAuthenticated()).toBeFalse();
      expect(service.roles()).toEqual([]);
      expect(service.isAdminAsUser()).toBeFalse();
      expect(service.isAdminRoleSelected()).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login'], { replaceUrl: true });
    });
  });

  describe('expiración del token', () => {
    const enSegundos = (delta: number) => Math.floor(Date.now() / 1000) + delta;

    it('cierra la sesión de inmediato si el token ya expiró', () => {
      iniciarSesion([ROLES.ADMIN], jwt({ exp: enSegundos(-10) }));
      expect(service.getToken()).toBeNull();
      expect(router.navigate).toHaveBeenCalled();
    });

    it('cierra la sesión 5 segundos antes de que expire', fakeAsync(() => {
      iniciarSesion([ROLES.ADMIN], jwt({ exp: enSegundos(3600) }));

      tick(3590 * 1000);
      expect(service.getToken()).not.toBeNull();

      tick(10 * 1000);
      expect(service.getToken()).toBeNull();
    }));

    it('no programa el cierre si el token no tiene expiración', fakeAsync(() => {
      iniciarSesion([ROLES.ADMIN], 'token-sin-exp');
      tick(24 * 3600 * 1000);
      expect(service.getToken()).toBe('token-sin-exp');
    }));
  });

  describe('loadFromStorage', () => {
    it('restaura la sesión con token y roles guardados', () => {
      localStorage.setItem('auth_token', 'abc');
      localStorage.setItem('auth_roles', JSON.stringify([ROLES.SEGURIDAD]));
      localStorage.setItem('admin_as_user', '1');

      service.loadFromStorage();

      expect(service.isAuthenticated()).toBeTrue();
      expect(service.hasRole(ROLES.SEGURIDAD)).toBeTrue();
      expect(service.isAdminAsUser()).toBeTrue();
    });

    it('tolera roles guardados con JSON inválido', () => {
      localStorage.setItem('auth_token', 'abc');
      localStorage.setItem('auth_roles', '{no-es-json');

      expect(() => service.loadFromStorage()).not.toThrow();
      expect(service.roles()).toEqual([]);
    });

    it('F06: sin token no considera autenticado al usuario', () => {
      service.loadFromStorage();

      expect(service.isAuthenticated()).toBeFalse();
      expect(service.roles()).toEqual([]);
      expect(localStorage.getItem('auth_roles')).toBeNull();
    });

    it('F06: sin token descarta los roles y el modo guardados', () => {
      localStorage.setItem('auth_roles', JSON.stringify([ROLES.SUPERADMIN]));
      localStorage.setItem('admin_as_user', '1');
      localStorage.setItem('admin_role_selected', '1');

      service.loadFromStorage();

      expect(service.isAuthenticated()).toBeFalse();
      expect(service.hasRole(ROLES.SUPERADMIN)).toBeFalse();
      expect(service.isAdminAsUser()).toBeFalse();
      expect(localStorage.getItem('auth_roles')).toBeNull();
      expect(localStorage.getItem('admin_as_user')).toBeNull();
      expect(localStorage.getItem('admin_role_selected')).toBeNull();
    });
  });

  describe('switchActiveRole', () => {
    it('ALL activa los cuatro roles', () => {
      service.switchActiveRole('ALL');
      expect(service.roles().length).toBe(4);
    });

    for (const rol of Object.values(ROLES)) {
      it(`${rol} deja únicamente ese rol y lo persiste`, () => {
        service.setAdminActingAsUser(true);

        service.switchActiveRole(rol);

        expect(service.roles()).toEqual([rol]);
        expect(JSON.parse(localStorage.getItem('auth_roles')!)).toEqual([rol]);
        expect(service.isAdminAsUser()).toBeFalse();
      });
    }

    it('ignora un rol desconocido', () => {
      service.switchActiveRole(ROLES.ADMIN);
      service.switchActiveRole('ROLE_X');
      expect(service.roles()).toEqual([ROLES.ADMIN]);
    });
  });

  describe('indicadores de administrador', () => {
    it('setAdminActingAsUser persiste y retira el indicador', () => {
      service.setAdminActingAsUser(true);
      expect(localStorage.getItem('admin_as_user')).toBe('1');

      service.setAdminActingAsUser(false);
      expect(localStorage.getItem('admin_as_user')).toBeNull();
    });

    it('setAdminRoleSelected persiste y retira el indicador', () => {
      service.setAdminRoleSelected(true);
      expect(service.isAdminRoleSelected()).toBeTrue();

      service.setAdminRoleSelected(false);
      expect(service.isAdminRoleSelected()).toBeFalse();
    });
  });

  describe('buildDisplayName', () => {
    const conRol = (rol: string) => (service as any).rolesSignal.set([rol]);

    it('antepone el prefijo según el rol', () => {
      conRol(ROLES.SUPERADMIN);
      expect(service.buildDisplayName('Ana')).toBe('superadmin. Ana');
      conRol(ROLES.ADMIN);
      expect(service.buildDisplayName('Ana')).toBe('Admin. Ana');
      conRol(ROLES.SEGURIDAD);
      expect(service.buildDisplayName('Ana')).toBe('seg. Ana');
    });

    it('distingue alumno y docente sin importar mayúsculas', () => {
      conRol(ROLES.USUARIO);
      expect(service.buildDisplayName('Juan Pérez', 'ALUMNO')).toBe('alum. Juan Pérez');
      expect(service.buildDisplayName('Juan Pérez', 'docente')).toBe('doc. Juan Pérez');
    });

    it('usa el prefijo Admin cuando actúa como usuario', () => {
      conRol(ROLES.USUARIO);
      service.setAdminActingAsUser(true);
      expect(service.buildDisplayName('Ana', 'ALUMNO')).toBe('Admin. Ana');
    });

    it('devuelve el nombre sin prefijo si no aplica ninguno', () => {
      conRol(ROLES.USUARIO);
      expect(service.buildDisplayName('Ana')).toBe('Ana');
    });

    it('recorta espacios y usa "Usuario" si no hay nombre', () => {
      expect(service.buildDisplayName('  Ana  ')).toBe('Ana');
      expect(service.buildDisplayName('   ')).toBe('Usuario');
    });
  });
});
