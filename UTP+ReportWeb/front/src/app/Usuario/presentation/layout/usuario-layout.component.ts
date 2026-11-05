import { Location } from '@angular/common';
import { Component, OnInit, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { NotificacionesFacade } from '../../application/notificaciones.facade';

@Component({
  selector: 'app-usuario-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './usuario-layout.component.html',
  styleUrls: ['../shared/usuario-ui.scss', './usuario-layout.component.scss'],
})
export class UsuarioLayoutComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly auth = inject(AuthService);
  private readonly notificaciones = inject(NotificacionesFacade);

  readonly enlaces = [
    { ruta: '/usuario', etiqueta: 'Inicio', icono: 'bi-house', exacto: true },
    { ruta: '/usuario/nuevo-reporte', etiqueta: 'Reportar', icono: 'bi-plus-circle', exacto: false },
    { ruta: '/usuario/estado-zonas', etiqueta: 'Zonas', icono: 'bi-geo-alt', exacto: false },
    { ruta: '/usuario/mis-reportes', etiqueta: 'Mis reportes', icono: 'bi-file-earmark-text', exacto: false },
  ];

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map(() => this.router.url),
      startWith(this.router.url),
    ),
    { requireSync: true },
  );

  readonly esInicio = computed(() => this.url().split(/[?#]/)[0].replace(/\/+$/, '') === '/usuario');
  readonly noLeidas = this.notificaciones.noLeidas;

  ngOnInit(): void {
    this.notificaciones.cargar();
  }

  volver(): void {
    this.location.back();
  }

  cerrarSesion(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
