import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { PerfilService, PerfilUsuario } from './perfil.service';

describe('PerfilService', () => {
  const URL = 'http://localhost:8080/api/usuarios/me';
  let service: PerfilService;
  let auth: jasmine.SpyObj<AuthService>;
  let httpMock: HttpTestingController;

  const perfilReal: PerfilUsuario = { username: 'ana', nombreCompleto: 'Ana Ruiz', roles: ['ROLE_USUARIO'] };

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: AuthService, useValue: auth }],
    });
    service = TestBed.inject(PerfilService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('inicia sin perfil', () => {
    expect(service.perfil()).toBeNull();
  });

  it('cargarPerfil guarda el perfil devuelto por el backend', () => {
    auth.getToken.and.returnValue('abc');

    service.cargarPerfil();
    httpMock.expectOne(URL).flush(perfilReal);

    expect(service.perfil()).toEqual(perfilReal);
  });

  it('obtenerPerfil devuelve el perfil sin modificar la señal', () => {
    let recibido: PerfilUsuario | undefined;

    service.obtenerPerfil().subscribe(p => (recibido = p));
    httpMock.expectOne(URL).flush(perfilReal);

    expect(recibido).toEqual(perfilReal);
    expect(service.perfil()).toBeNull();
  });

  it('F05: sin token no asigna ningún perfil ni llama al backend', () => {
    auth.getToken.and.returnValue(null);

    service.cargarPerfil();

    httpMock.expectNone(URL);
    expect(service.perfil()).toBeNull();
  });

  it('F05: si el backend falla no asigna ningún perfil', () => {
    auth.getToken.and.returnValue('abc');

    service.cargarPerfil();
    httpMock.expectOne(URL).flush(null, { status: 500, statusText: 'Error' });

    expect(service.perfil()).toBeNull();
  });

  it('F05: si el backend falla no conserva el perfil anterior', () => {
    auth.getToken.and.returnValue('abc');
    service.cargarPerfil();
    httpMock.expectOne(URL).flush(perfilReal);

    service.cargarPerfil();
    httpMock.expectOne(URL).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(service.perfil()).toBeNull();
  });
});
