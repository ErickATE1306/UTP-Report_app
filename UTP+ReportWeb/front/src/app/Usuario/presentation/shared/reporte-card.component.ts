import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReporteDetallado } from '../../domain/models/reporte.model';
import { codigoReporte, etiquetaEstado, progresoEstado } from '../../domain/rules/reporte.rules';
import { FechaPipe, tonoEstado } from './presentacion';

@Component({
  selector: 'app-reporte-card',
  standalone: true,
  imports: [RouterLink, FechaPipe],
  template: `
    <a class="u-card tarjeta" [routerLink]="['/usuario/mis-reportes', reporte().id]">
      <span class="u-icono" aria-hidden="true"><i class="bi bi-file-earmark-text"></i></span>
      <span class="cuerpo">
        <span class="cabecera">
          <span class="codigo">{{ codigo() }}</span>
          <span class="u-chip" [class]="'u-tono-' + tono()">{{ estado() }}</span>
        </span>
        <strong>{{ reporte().tipoNombre }}</strong>
        <span class="u-tenue datos">
          <span><i class="bi bi-calendar3" aria-hidden="true"></i> {{ reporte().fechaCreacion | fecha }}</span>
          <span><i class="bi bi-geo-alt" aria-hidden="true"></i> {{ reporte().zonaNombre }}</span>
        </span>
        <span class="u-progreso" role="progressbar" aria-label="Avance del reporte"
          [attr.aria-valuenow]="progreso()" aria-valuemin="0" aria-valuemax="100">
          <span [style.width.%]="progreso()"></span>
        </span>
      </span>
      <i class="bi bi-chevron-right flecha" aria-hidden="true"></i>
    </a>
  `,
  styleUrls: ['./usuario-ui.scss'],
  styles: [`
    .tarjeta { display: flex; align-items: center; gap: 14px; color: inherit; text-decoration: none; transition: border-color .15s; }
    .tarjeta:hover { border-color: var(--u-indigo); }
    .cuerpo { display: grid; flex: 1; gap: 6px; min-width: 0; }
    .cabecera { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 6px; }
    .codigo { color: var(--u-indigo); font-size: .8rem; font-weight: 600; }
    .datos { display: flex; flex-wrap: wrap; gap: 4px 14px; font-size: .8rem; }
    .flecha { flex: none; color: var(--u-tenue); }
  `],
})
export class ReporteCardComponent {
  readonly reporte = input.required<ReporteDetallado>();
  readonly codigo = computed(() => codigoReporte(this.reporte().id));
  readonly estado = computed(() => etiquetaEstado(this.reporte().estado));
  readonly progreso = computed(() => progresoEstado(this.reporte().estado));
  readonly tono = computed(() => tonoEstado(this.reporte().estado));
}
