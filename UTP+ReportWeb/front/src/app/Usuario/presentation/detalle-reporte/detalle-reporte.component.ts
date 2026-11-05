import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ObtenerDetalleReporteUseCase } from '../../application/mis-reportes.use-cases';
import { ReporteDetallado } from '../../domain/models/reporte.model';
import {
  codigoReporte,
  construirSeguimiento,
  etiquetaEstado,
  etiquetaPrioridad,
  progresoEstado,
} from '../../domain/rules/reporte.rules';
import { FechaPipe, mensajeDeError } from '../shared/presentacion';

@Component({
  selector: 'app-detalle-reporte',
  standalone: true,
  imports: [RouterLink, FechaPipe],
  templateUrl: './detalle-reporte.component.html',
  styleUrls: ['../shared/usuario-ui.scss', './detalle-reporte.component.scss'],
})
export class DetalleReporteComponent implements OnInit {
  private readonly detalle = inject(ObtenerDetalleReporteUseCase);

  readonly id = input.required<string>();

  readonly reporte = signal<ReporteDetallado | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly sinPermiso = signal(false);

  readonly codigo = computed(() => codigoReporte(Number(this.id())));
  readonly estado = computed(() => {
    const reporte = this.reporte();
    return reporte ? etiquetaEstado(reporte.estado) : '';
  });
  readonly progreso = computed(() => {
    const reporte = this.reporte();
    return reporte ? progresoEstado(reporte.estado) : 0;
  });
  readonly prioridad = computed(() => etiquetaPrioridad(this.reporte()?.prioridad ?? null));
  readonly seguimiento = computed(() => {
    const reporte = this.reporte();
    return reporte ? construirSeguimiento(reporte.estado) : [];
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.detalle.ejecutar(Number(this.id())).subscribe({
      next: (reporte) => {
        this.reporte.set(reporte);
        this.cargando.set(false);
      },
      error: (error: unknown) => {
        this.error.set(mensajeDeError(error, 'No pudimos cargar este reporte.'));
        this.sinPermiso.set(error instanceof HttpErrorResponse && error.status === 403);
        this.cargando.set(false);
      },
    });
  }
}
