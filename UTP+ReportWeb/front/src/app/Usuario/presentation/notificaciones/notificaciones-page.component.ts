import { Component, OnInit, inject } from '@angular/core';
import { NotificacionesFacade } from '../../application/notificaciones.facade';
import { NotificacionesComponent } from './notificaciones.component';

@Component({
  selector: 'app-notificaciones-page',
  standalone: true,
  imports: [NotificacionesComponent],
  template: `
    <div class="u-pagina">
      @if (facade.error()) {
        <div class="u-card u-estado" role="alert">
          <p>No pudimos cargar tus notificaciones.</p>
          <button type="button" class="u-btn u-btn--secundario" (click)="facade.cargar()">Reintentar</button>
        </div>
      } @else if (facade.cargando() && !facade.items().length) {
        <p class="u-estado" role="status">Cargando notificaciones…</p>
      } @else {
        <app-notificaciones [items]="facade.items()" [permitirMarcar]="true" (marcarTodas)="facade.marcarTodasLeidas()" />
      }
    </div>
  `,
  styleUrls: ['../shared/usuario-ui.scss'],
})
export class NotificacionesPageComponent implements OnInit {
  readonly facade = inject(NotificacionesFacade);

  ngOnInit(): void {
    this.facade.cargar();
  }
}
