import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('agrega el encabezado Authorization cuando hay token', () => {
    localStorage.setItem('auth_token', 'abc');

    http.get('/api/reportes').subscribe();

    const req = httpMock.expectOne('/api/reportes');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush([]);
  });

  it('no agrega el encabezado cuando no hay token', () => {
    http.get('/api/reportes').subscribe();

    const req = httpMock.expectOne('/api/reportes');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush([]);
  });

  it('no agrega el encabezado en el login', () => {
    localStorage.setItem('auth_token', 'abc');

    http.post('/api/auth/login', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('agrega el encabezado en los registros, que requieren SUPERADMIN', () => {
    localStorage.setItem('auth_token', 'abc');

    http.post('/api/auth/registrarAdmin', {}).subscribe();

    const req = httpMock.expectOne('/api/auth/registrarAdmin');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush('ok');
  });
});
