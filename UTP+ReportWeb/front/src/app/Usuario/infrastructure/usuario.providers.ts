import { Provider } from '@angular/core';
import {
  CatalogoRepository,
  NotificacionesLeidasStore,
  PerfilRepository,
  ReporteRepository,
  ZonaRepository,
} from '../domain/ports/usuario.ports';
import {
  HttpCatalogoRepository,
  HttpPerfilRepository,
  HttpReporteRepository,
  HttpZonaRepository,
} from './http-usuario.repositories';
import { LocalNotificacionesLeidasStore } from './local-notificaciones-leidas.store';

/** Raíz de composición: único punto donde los puertos se enlazan con sus adaptadores. */
export function provideUsuarioModule(): Provider[] {
  return [
    { provide: ReporteRepository, useClass: HttpReporteRepository },
    { provide: ZonaRepository, useClass: HttpZonaRepository },
    { provide: CatalogoRepository, useClass: HttpCatalogoRepository },
    { provide: PerfilRepository, useClass: HttpPerfilRepository },
    { provide: NotificacionesLeidasStore, useClass: LocalNotificacionesLeidasStore },
  ];
}
