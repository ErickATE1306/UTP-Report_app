import { Injectable, computed, inject, signal } from '@angular/core';
import { NotificacionItem } from '../domain/models/notificacion.model';
import { NotificacionesLeidasStore } from '../domain/ports/usuario.ports';
import { claveNotificacion, reporteANotificacion } from '../domain/rules/notificacion.rules';
import { ObtenerMisReportesUseCase } from './mis-reportes.use-cases';

/** Estado compartido entre la campana del encabezado y la página de notificaciones. */
@Injectable({ providedIn: 'root' })
export class NotificacionesFacade {
  private readonly misReportes = inject(ObtenerMisReportesUseCase);
  private readonly leidas = inject(NotificacionesLeidasStore);

  readonly items = signal<NotificacionItem[]>([]);
  readonly cargando = signal(false);
  readonly error = signal(false);
  readonly noLeidas = computed(() => this.items().filter((item) => !item.read).length);

  cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.misReportes.ejecutar().subscribe({
      next: (reportes) => {
        const leidas = this.leidas.leer();
        this.items.set(reportes.map((reporte) => reporteANotificacion(reporte, leidas)));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  marcarTodasLeidas(): void {
    this.leidas.guardar(new Set(this.items().map(claveNotificacion)));
    this.items.update((items) => items.map((item) => ({ ...item, read: true })));
  }
}
