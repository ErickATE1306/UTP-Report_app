import { Component, computed, input, output, signal } from '@angular/core';
import { FiltroNotificacion, NotificacionItem } from '../../domain/models/notificacion.model';
import { agruparPorDia, filtrarNotificaciones } from '../../domain/rules/notificacion.rules';
import { FechaPipe } from '../shared/presentacion';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [FechaPipe],
  template: `
    <div class="cabecera">
      <h2>Notificaciones</h2>
      @if (permitirMarcar() && hayNoLeidas()) {
        <button type="button" class="marcar" (click)="marcarTodas.emit()">
          Marcar todo como leído <i class="bi bi-check-circle" aria-hidden="true"></i>
        </button>
      }
    </div>

    <div class="filtros" role="group" aria-label="Filtrar notificaciones">
      @for (opcion of opciones; track opcion.valor) {
        <button type="button" [class.activo]="filtro() === opcion.valor" [attr.aria-pressed]="filtro() === opcion.valor"
          (click)="filtro.set(opcion.valor)">{{ opcion.etiqueta }}</button>
      }
    </div>

    @for (grupo of grupos(); track grupo.titulo) {
      <section class="grupo">
        <h3>{{ grupo.titulo }}</h3>
        <ul>
          @for (item of grupo.items; track item.id + item.ts.getTime()) {
            <li class="u-card item">
              <span class="u-icono" aria-hidden="true">
                <i class="bi" [class]="item.zonaNombre ? 'bi-geo-alt' : 'bi-clipboard-check'"></i>
              </span>
              <div class="item__cuerpo">
                <strong>{{ item.title }}</strong>
                <p class="u-tenue">{{ item.body }}</p>
              </div>
              <span class="item__hora">
                @if (!item.read) { <span class="punto"><span class="lector">Sin leer</span></span> }
                <time class="u-tenue">{{ item.ts | fecha: 'hora' }}</time>
              </span>
            </li>
          }
        </ul>
      </section>
    } @empty {
      <div class="u-estado">
        <i class="bi bi-bell-slash" aria-hidden="true"></i>
        <p>No tienes notificaciones.</p>
      </div>
    }
  `,
  styleUrls: ['../shared/usuario-ui.scss'],
  styles: [`
    :host { display: grid; gap: 14px; }
    h2 { font-size: 1.25rem; }
    h3 { margin-bottom: 8px; font-size: .95rem; font-weight: 600; }
    .cabecera { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; }
    .marcar { padding: 8px 0; border: 0; background: none; color: var(--u-indigo); font: inherit; font-size: .85rem; font-weight: 500; cursor: pointer; }
    .filtros { display: flex; flex-wrap: wrap; gap: 8px; }
    .filtros button { min-height: 36px; padding: 6px 16px; border: 1px solid var(--u-borde); border-radius: 999px; background: #fff; color: var(--u-texto); font: inherit; font-size: .85rem; cursor: pointer; }
    .filtros .activo { border-color: var(--u-indigo); background: var(--u-indigo-suave); color: var(--u-indigo); font-weight: 600; }
    ul { display: grid; gap: 10px; margin: 0; padding: 0; list-style: none; }
    .item { display: flex; align-items: center; gap: 12px; padding: 12px; }
    .item__cuerpo { display: grid; flex: 1; gap: 2px; min-width: 0; overflow-wrap: anywhere; }
    .item__hora { display: flex; flex: none; align-items: center; gap: 6px; font-size: .8rem; }
    .item__hora .u-tenue { font-size: inherit; }
    .punto { width: 8px; height: 8px; border-radius: 50%; background: var(--u-coral); }
    .lector { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); }
  `],
})
export class NotificacionesComponent {
  readonly items = input<NotificacionItem[]>([]);
  /** El panel del navbar marca como leído al abrirse; la página completa muestra esta acción. */
  readonly permitirMarcar = input(false);
  readonly marcarTodas = output<void>();

  readonly opciones: ReadonlyArray<{ valor: FiltroNotificacion; etiqueta: string }> = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'REPORTES', etiqueta: 'Reportes' },
    { valor: 'ZONAS', etiqueta: 'Zonas' },
  ];
  readonly filtro = signal<FiltroNotificacion>('TODAS');
  readonly hayNoLeidas = computed(() => this.items().some((item) => !item.read));
  readonly grupos = computed(() => agruparPorDia(filtrarNotificaciones(this.items(), this.filtro()), new Date()));
}
