import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({ selector: 'app-admin-layout', standalone: true, imports: [RouterLink, RouterOutlet], template: `
  <div class="layout" [class.dark]="darkMode()"><aside><a routerLink="/admin" class="brand">UTP Reporta</a><nav><a routerLink="/admin">Panel</a></nav><button type="button" (click)="toggleTheme()">{{ darkMode() ? 'Modo claro' : 'Modo oscuro' }}</button><button type="button" (click)="logout()">Cerrar sesión</button></aside><section><header><span>Administración</span></header><main><router-outlet /></main></section></div>`, styles: [`:host{display:block}.layout{min-height:100vh;display:grid;grid-template-columns:240px 1fr;background:#f6f8fc;color:#182238}.layout.dark{background:#121826;color:#f8fafc}.layout.dark aside,.layout.dark header{background:#1d2638;color:#f8fafc}aside{padding:24px;background:#182238;color:#fff;display:flex;flex-direction:column;gap:16px}.brand{font-weight:800;font-size:1.2rem}a{color:inherit;text-decoration:none}nav a{display:block;padding:10px 0}button{padding:10px;border-radius:8px;border:1px solid currentColor;background:transparent;color:inherit;cursor:pointer}section{min-width:0}header{height:64px;display:flex;align-items:center;padding:0 28px;background:#fff;font-weight:700}main{padding:0}@media(max-width:700px){.layout{grid-template-columns:1fr}aside{flex-direction:row;align-items:center;flex-wrap:wrap;padding:14px}.brand{margin-right:auto}header{height:52px}}`] })
export class AdminLayoutComponent {
  readonly darkMode = signal(localStorage.getItem('admin_dark_mode') === 'true');
  constructor(private readonly auth: AuthService, private readonly router: Router) {}
  toggleTheme(): void { this.darkMode.update((active) => { localStorage.setItem('admin_dark_mode', String(!active)); return !active; }); }
  logout(): void { this.auth.logout(); void this.router.navigateByUrl('/login'); }
}
