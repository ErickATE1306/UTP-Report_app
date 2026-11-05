import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ObtenerMisReportesUseCase } from '../../application/mis-reportes.use-cases';
import { FiltroReporte, ReporteDetallado } from '../../domain/models/reporte.model';
import { contarReportes, filtrarReportes } from '../../domain/rules/reporte.rules';
import { ReporteCardComponent } from '../shared/reporte-card.component';

@Component({
  selector: 'app-mis-reportes',
  standalone: true,
  imports: [FormsModule, RouterLink, ReporteCardComponent],
  template: `
    <div class="u-pagina">
      <h1>Mis reportes</h1>

      <div class="filtros" role="group" aria-label="Filtrar reportes">
        @for (opcion of opciones(); track opcion.valor) {
          <button type="button" class="u-card filtro" [class.activo]="filtro() === opcion.valor"
            [attr.aria-pressed]="filtro() === opcion.valor" (click)="filtro.set(opcion.valor)">
            <span class="u-tenue">{{ opcion.etiqueta }}</span>
            <strong>{{ opcion.cantidad }}</strong>
          </button>
        }
      </div>

      <label class="u-buscador">
        <i class="bi bi-search" aria-hidden="true"></i>
        <input class="u-input" type="search" name="busqueda" placeholder="Buscar reporte" aria-label="Buscar reporte"
          [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)">
      </label>

      @if (cargando()) {
        <p class="u-estado" role="status">Cargando tus reportes…</p>
      } @else if (error()) {
        <div class="u-card u-estado" role="alert">
          <p>No pudimos cargar tus reportes.</p>
          <button type="button" class="u-btn u-btn--secundario" (click)="cargar()">Reintentar</button>
        </div>
      } @else {
        <div class="lista">
          @for (reporte of visibles(); track reporte.id) {
            <app-reporte-card [reporte]="reporte" />
          } @empty {
            <div class="u-card u-estado">
              <i class="bi bi-inbox" aria-hidden="true"></i>
              @if (reportes().length) {
                <p>Ningún reporte coincide con tu búsqueda.</p>
              } @else {
                <p>Aún no has enviado reportes.</p>
                <a class="u-btn" routerLink="/usuario/nuevo-reporte">Crear reporte</a>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styleUrls: ['../shared/usuario-ui.scss'],
  styles: [`
    .filtros { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
    .filtro { display: grid; gap: 2px; padding: 12px; font: inherit; text-align: left; cursor: pointer; }
    .filtro strong { font-size: 1.25rem; }
    .filtro.activo { border: 2px solid var(--u-indigo); padding: 11px; }
    .lista { display: grid; gap: 12px; }
    @media (min-width: 900px) { .lista { grid-template-columns: 1fr 1fr; } .lista .u-estado { grid-column: 1 / -1; } }
  `],
})
export class MisReportesComponent implements OnInit {
  private readonly misReportes = inject(ObtenerMisReportesUseCase);

  readonly reportes = signal<ReporteDetallado[]>([]);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly filtro = signal<FiltroReporte>('TODOS');
  readonly busqueda = signal('');

  readonly opciones = computed(() => {
    const { total, activos, resueltos } = contarReportes(this.reportes());
    return [
      { valor: 'TODOS' as const, etiqueta: 'Todos', cantidad: total },
      { valor: 'ACTIVOS' as const, etiqueta: 'Activos', cantidad: activos },
      { valor: 'RESUELTOS' as const, etiqueta: 'Resueltos', cantidad: resueltos },
    ];
  });
  readonly visibles = computed(() => filtrarReportes(this.reportes(), this.filtro(), this.busqueda()));

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.misReportes.ejecutar().subscribe({
      next: (reportes) => {
        this.reportes.set(reportes);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }
}
