import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({ selector: 'app-login', standalone: true, imports: [CommonModule, FormsModule], template: `<main><form (ngSubmit)="submit()"><p class="eyebrow">UTP Reporta</p><h1>Acceso administrativo</h1><p>Ingresa con una cuenta que tenga rol administrador.</p><label>Usuario o correo<input name="username" [(ngModel)]="username" autocomplete="username" required></label><label>Contraseña<input name="password" [(ngModel)]="password" type="password" autocomplete="current-password" required></label><p class="error" *ngIf="error()">{{ error() }}</p><button [disabled]="loading()">{{ loading() ? 'Ingresando…' : 'Ingresar' }}</button></form></main>`, styles: [`main{min-height:100vh;display:grid;place-items:center;background:#f6f8fc;padding:20px}form{display:grid;gap:14px;width:min(100%,420px);padding:32px;background:#fff;border-radius:16px;box-shadow:0 12px 36px #18223818}h1,p{margin:0}.eyebrow{color:#d61d42;font-weight:800;text-transform:uppercase;font-size:.8rem;letter-spacing:.1em}label{display:grid;gap:6px;font-weight:700}input{padding:11px;border:1px solid #cbd5e1;border-radius:8px;font:inherit}button{padding:12px;border:0;border-radius:8px;background:#d61d42;color:#fff;font-weight:800;cursor:pointer}.error{color:#b91c1c}`] })
export class LoginComponent {
  username = ''; password = ''; readonly loading = signal(false); readonly error = signal<string | null>(null);
  constructor(private readonly auth: AuthService, private readonly router: Router) {}
  async submit(): Promise<void> { if (!this.username || !this.password) return; this.loading.set(true); this.error.set(null); try { await this.auth.login(this.username, this.password); if (!this.auth.hasRole('ROLE_ADMIN')) { this.auth.logout(); throw new Error('Esta cuenta no tiene permiso de administrador.'); } await this.router.navigateByUrl('/admin'); } catch (error: unknown) { this.error.set(error instanceof Error ? error.message : 'No se pudo iniciar sesión.'); } finally { this.loading.set(false); } }
}
