import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let roles: string[];
  let navegar: jasmine.Spy;
  const auth = {
    login: jasmine.createSpy('login'),
    logout: jasmine.createSpy('logout'),
    hasRole: (rol: string) => roles.includes(rol),
  };

  async function ingresar(): Promise<LoginComponent> {
    const componente = TestBed.createComponent(LoginComponent).componentInstance;
    componente.username = 'ana';
    componente.password = 'clave';
    await componente.submit();
    return componente;
  }

  beforeEach(() => {
    auth.login.calls.reset();
    auth.logout.calls.reset();
    auth.login.and.resolveTo();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
    navegar = spyOn(TestBed.inject(Router), 'navigateByUrl').and.resolveTo(true);
  });

  it('envía al superadministrador a su panel', async () => {
    roles = ['ROLE_SUPERADMIN'];
    await ingresar();
    expect(navegar).toHaveBeenCalledOnceWith('/superadmin/dashboard');
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('envía al administrador a /admin, aunque también sea usuario', async () => {
    roles = ['ROLE_USUARIO', 'ROLE_ADMIN'];
    await ingresar();
    expect(navegar).toHaveBeenCalledOnceWith('/admin');
  });

  it('envía al usuario a /usuario', async () => {
    roles = ['ROLE_USUARIO'];
    await ingresar();
    expect(navegar).toHaveBeenCalledOnceWith('/usuario');
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('rechaza otros roles y cierra la sesión', async () => {
    roles = ['ROLE_SEGURIDAD'];
    const componente = await ingresar();
    expect(navegar).not.toHaveBeenCalled();
    expect(auth.logout).toHaveBeenCalledTimes(1);
    expect(componente.error()).toBe('Esta cuenta no tiene acceso a este portal.');
  });

  it('muestra el error de credenciales', async () => {
    auth.login.and.rejectWith(new Error('Credenciales inválidas'));
    const componente = await ingresar();
    expect(componente.error()).toBe('Credenciales inválidas');
    expect(componente.loading()).toBeFalse();
  });

  it('explica que la cuenta está bloqueada (B08)', async () => {
    const cuerpo = new Blob([JSON.stringify({ message: 'La cuenta está bloqueada. Inténtelo de nuevo después de 14 minutos.' })]);
    auth.login.and.rejectWith(new HttpErrorResponse({ status: 403, error: cuerpo }));
    const componente = await ingresar();
    expect(componente.error()).toContain('bloqueada');
    expect(componente.error()).toContain('14 minutos');
  });

  it('no revela detalles con otras respuestas del login', async () => {
    auth.login.and.rejectWith(new HttpErrorResponse({ status: 401, error: { message: 'Credenciales inválidas.' } }));
    const componente = await ingresar();
    expect(componente.error()).toBe('No se pudo iniciar sesión.');
  });

  it('no intenta iniciar sesión sin datos', async () => {
    const componente = TestBed.createComponent(LoginComponent).componentInstance;
    await componente.submit();
    expect(auth.login).not.toHaveBeenCalled();
  });
});
