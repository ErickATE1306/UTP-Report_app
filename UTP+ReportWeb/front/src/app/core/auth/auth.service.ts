import { Injectable, computed, signal } from '@angular/core';
import { AuthControllerService } from '../../api/services/auth-controller.service';

interface JwtPayload { exp?: number; roles?: string[]; }
interface LoginResponse { token?: string; roles?: string[]; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly rolesState = signal<string[]>(this.readRoles());
  readonly roles = computed(() => this.rolesState());

  constructor(private readonly authApi: AuthControllerService) {}

  async login(usernameOrCorreo: string, password: string): Promise<void> {
    const response = await this.authApi.login$Response({ body: { usernameOrCorreo, password } });
    const body = response.body;
    const parsed = body instanceof Blob ? JSON.parse(await body.text()) as LoginResponse : body as LoginResponse;
    if (!parsed.token) throw new Error('El servidor no devolvió un token de acceso.');
    const roles = parsed.roles ?? this.decode(parsed.token)?.roles ?? [];
    localStorage.setItem('auth_token', parsed.token);
    localStorage.setItem('auth_roles', JSON.stringify(roles));
    this.rolesState.set(roles);
  }

  logout(): void {
    localStorage.removeItem('auth_token'); localStorage.removeItem('auth_roles');
    this.rolesState.set([]);
  }

  hasRole(role: string): boolean { return this.rolesState().includes(role); }
  token(): string | null { return localStorage.getItem('auth_token'); }

  /** Se evalúa en cada llamada: el token vive en localStorage y también expira con el tiempo. */
  isAuthenticated(): boolean {
    const exp = this.decode(this.token() ?? '')?.exp;
    return typeof exp === 'number' && Date.now() < exp * 1000;
  }

  private readRoles(): string[] {
    try { return JSON.parse(localStorage.getItem('auth_roles') ?? '[]') as string[]; } catch { return []; }
  }
  private decode(token: string): JwtPayload | null {
    try { return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as JwtPayload; } catch { return null; }
  }
}
