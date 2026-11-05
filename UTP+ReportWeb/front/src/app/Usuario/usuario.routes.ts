import { Routes } from '@angular/router';
import { usuarioGuard } from './presentation/usuario.guard';
import { UsuarioLayoutComponent } from './presentation/layout/usuario-layout.component';

export const USUARIO_ROUTES: Routes = [
  {
    path: '',
    canActivate: [usuarioGuard],
    component: UsuarioLayoutComponent,
    children: [
      { path: '', pathMatch: 'full', loadComponent: () => import('./presentation/inicio/inicio-usuario.component').then((m) => m.InicioUsuarioComponent) },
      { path: 'nuevo-reporte', loadComponent: () => import('./presentation/nuevo-reporte/nuevo-reporte.component').then((m) => m.NuevoReporteComponent) },
      { path: 'mis-reportes', loadComponent: () => import('./presentation/mis-reportes/mis-reportes.component').then((m) => m.MisReportesComponent) },
      { path: 'mis-reportes/:id', loadComponent: () => import('./presentation/detalle-reporte/detalle-reporte-route.component').then((m) => m.DetalleReporteRouteComponent) },
      { path: 'estado-zonas', loadComponent: () => import('./presentation/estado-zonas/zonas-page-complete.component').then((m) => m.ZonasPageCompleteComponent) },
      { path: 'notificaciones', loadComponent: () => import('./presentation/notificaciones/notificaciones-page.component').then((m) => m.NotificacionesPageComponent) },
      { path: 'guia-page', loadComponent: () => import('./presentation/guia/guia-seguridad.component').then((m) => m.GuiaSeguridadComponent) },
    ],
  },
];
