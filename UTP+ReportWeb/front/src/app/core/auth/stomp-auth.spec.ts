import { encabezadosStomp } from './stomp-auth';

describe('encabezadosStomp', () => {
  beforeEach(() => localStorage.removeItem('auth_token'));
  afterEach(() => localStorage.removeItem('auth_token'));

  it('envía el token guardado', () => {
    localStorage.setItem('auth_token', 'abc');
    expect(encabezadosStomp()).toEqual({ Authorization: 'Bearer abc' });
  });

  it('devuelve null sin sesión', () => {
    expect(encabezadosStomp()).toBeNull();
  });
});
