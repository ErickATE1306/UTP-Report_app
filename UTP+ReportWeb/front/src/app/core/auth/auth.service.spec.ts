import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { AuthControllerService } from '../../api/services/auth-controller.service';

/** Construye un JWT sin firma válida: el servicio solo lee el payload. */
function jwt(payload: object): string {
  const b64 = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `header.${b64}.firma`;
}
const enSegundos = (delta: number) => Math.floor(Date.now() / 1000) + delta;

describe('core/AuthService', () => {
  let api: jasmine.SpyObj<AuthControllerService>;

  function crearServicio(): AuthService {
    return TestBed.inject(AuthService);
  }

  beforeEach(() => {
    localStorage.clear();
    api = jasmine.createSpyObj<AuthControllerService>('AuthControllerService', ['login$Response']);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthControllerService, useValue: api }],
    });
  });

  afterEach(() => localStorage.clear());

  describe('login', () => {
    it('envía las credenciales y guarda token y roles', async () => {
      api.login$Response.and.resolveTo({ body: { token: 'abc', roles: ['ROLE_ADMIN'] } } as any);
      const service = crearServicio();

      await service.login('ana', 'secreto');

      expect(api.login$Response).toHaveBeenCalledOnceWith({ body: { usernameOrCorreo: 'ana', password: 'secreto' } });
      expect(service.token()).toBe('abc');
      expect(localStorage.getItem('auth_roles')).toBe('["ROLE_ADMIN"]');
      expect(service.hasRole('ROLE_ADMIN')).toBeTrue();
    });

    it('interpreta la respuesta cuando llega como Blob', async () => {
      const blob = new Blob([JSON.stringify({ token: 'desde-blob', roles: ['ROLE_USUARIO'] })], { type: 'application/json' });
      api.login$Response.and.resolveTo({ body: blob } as any);
      const service = crearServicio();

      await service.login('ana', 'secreto');

      expect(service.token()).toBe('desde-blob');
      expect(service.roles()).toEqual(['ROLE_USUARIO']);
    });

    it('obtiene los roles desde el JWT si la respuesta no los incluye', async () => {
      const token = jwt({ roles: ['ROLE_SEGURIDAD'] });
      api.login$Response.and.resolveTo({ body: { token } } as any);
      const service = crearServicio();

      await service.login('ana', 'secreto');

      expect(service.roles()).toEqual(['ROLE_SEGURIDAD']);
    });

    it('deja los roles vacíos si no vienen en la respuesta ni en el JWT', async () => {
      api.login$Response.and.resolveTo({ body: { token: 'token-no-decodificable' } } as any);
      const service = crearServicio();

      await service.login('ana', 'secreto');

      expect(service.roles()).toEqual([]);
    });

    it('lanza un error y no guarda nada si el servidor no devuelve token', async () => {
      api.login$Response.and.resolveTo({ body: {} } as any);
      const service = crearServicio();

      await expectAsync(service.login('ana', 'secreto')).toBeRejectedWithError('El servidor no devolvió un token de acceso.');
      expect(service.token()).toBeNull();
    });

    it('propaga el error cuando la API rechaza las credenciales', async () => {
      api.login$Response.and.rejectWith(new Error('401'));
      const service = crearServicio();

      await expectAsync(service.login('ana', 'mala')).toBeRejected();
      expect(service.token()).toBeNull();
    });
  });

  describe('logout', () => {
    it('elimina token y roles', async () => {
      api.login$Response.and.resolveTo({ body: { token: 'abc', roles: ['ROLE_ADMIN'] } } as any);
      const service = crearServicio();
      await service.login('ana', 'secreto');

      service.logout();

      expect(service.token()).toBeNull();
      expect(localStorage.getItem('auth_roles')).toBeNull();
      expect(service.roles()).toEqual([]);
      expect(service.hasRole('ROLE_ADMIN')).toBeFalse();
    });
  });

  describe('estado inicial', () => {
    it('restaura los roles guardados en localStorage', () => {
      localStorage.setItem('auth_roles', '["ROLE_ADMIN"]');
      expect(crearServicio().hasRole('ROLE_ADMIN')).toBeTrue();
    });

    it('ignora roles guardados con JSON inválido', () => {
      localStorage.setItem('auth_roles', '{no-es-json');
      expect(crearServicio().roles()).toEqual([]);
    });
  });

  describe('isAuthenticated', () => {
    it('es falso sin token', () => {
      expect(crearServicio().isAuthenticated()).toBeFalse();
    });

    it('es verdadero con un token vigente', () => {
      localStorage.setItem('auth_token', jwt({ exp: enSegundos(3600) }));
      expect(crearServicio().isAuthenticated()).toBeTrue();
    });

    it('es falso con un token expirado', () => {
      localStorage.setItem('auth_token', jwt({ exp: enSegundos(-60) }));
      expect(crearServicio().isAuthenticated()).toBeFalse();
    });

    it('F01: pasa a verdadero después de iniciar sesión', async () => {
      api.login$Response.and.resolveTo({ body: { token: jwt({ exp: enSegundos(3600) }), roles: ['ROLE_ADMIN'] } } as any);
      const service = crearServicio();
      expect(service.isAuthenticated()).toBeFalse();

      await service.login('ana', 'secreto');

      expect(service.isAuthenticated()).toBeTrue();
    });

    it('F02: pasa a falso después de cerrar sesión', () => {
      localStorage.setItem('auth_token', jwt({ exp: enSegundos(3600) }));
      const service = crearServicio();
      expect(service.isAuthenticated()).toBeTrue();

      service.logout();

      expect(service.isAuthenticated()).toBeFalse();
    });

    it('deja de estar autenticado cuando el token expira, sin recargar', () => {
      jasmine.clock().install();
      try {
        jasmine.clock().mockDate(new Date(2026, 0, 1, 12, 0, 0));
        localStorage.setItem('auth_token', jwt({ exp: Date.now() / 1000 + 60 }));
        const service = crearServicio();
        expect(service.isAuthenticated()).toBeTrue();

        jasmine.clock().tick(61_000);

        expect(service.isAuthenticated()).toBeFalse();
      } finally {
        jasmine.clock().uninstall();
      }
    });

    it('es falso con un token sin fecha de expiración', () => {
      localStorage.setItem('auth_token', jwt({}));
      expect(crearServicio().isAuthenticated()).toBeFalse();
    });

    it('F03: es falso con un token que no se puede decodificar', () => {
      localStorage.setItem('auth_token', 'token-basura');
      expect(crearServicio().isAuthenticated()).toBeFalse();
    });
  });
});
