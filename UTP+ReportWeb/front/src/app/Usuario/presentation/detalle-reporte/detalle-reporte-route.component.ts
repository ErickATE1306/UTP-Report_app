import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DetalleReporteComponent } from './detalle-reporte.component';

/** Pasa el parámetro de ruta sin exigir withComponentInputBinding() en toda la aplicación. */
@Component({
  selector: 'app-detalle-reporte-route',
  standalone: true,
  imports: [DetalleReporteComponent],
  template: `<app-detalle-reporte [id]="id" />`,
})
export class DetalleReporteRouteComponent {
  readonly id = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';
}
