import { Component, HostListener, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ObtenerPerfilUseCase } from '../../application/catalogos.use-cases';
import { ObtenerMisReportesUseCase } from '../../application/mis-reportes.use-cases';
import { ReporteDetallado } from '../../domain/models/reporte.model';
import { contarReportes, esActivo } from '../../domain/rules/reporte.rules';
import { ReporteCardComponent } from '../shared/reporte-card.component';

@Component({
  selector: 'app-inicio-usuario',
  standalone: true,
  imports: [RouterLink, ReporteCardComponent],
  templateUrl: './inicio-usuario.component.html',
  styleUrls: ['../shared/usuario-ui.scss', './inicio-usuario.component.scss'],
})
export class InicioUsuarioComponent implements OnInit {
  private readonly perfil = inject(ObtenerPerfilUseCase);
  private readonly misReportes = inject(ObtenerMisReportesUseCase);
  private readonly router = inject(Router);

  readonly nombre = signal('');
  readonly reportes = signal<ReporteDetallado[]>([]);
  readonly cargando = signal(true);
  readonly error = signal(false);
  readonly conteo = computed(() => contarReportes(this.reportes()));
  /** Prioriza los casos en curso; sin ninguno, muestra los más recientes. */
  readonly destacados = computed(() => {
    const activos = this.reportes().filter((reporte) => esActivo(reporte.estado));
    return (activos.length ? activos : this.reportes()).slice(0, 2);
  });

  ngOnInit(): void {
    this.perfil.ejecutar().subscribe({
      next: ({ nombreCompleto }) => this.nombre.set(nombreCompleto?.trim().split(/\s+/)[0] ?? ''),
      error: () => this.nombre.set(''),
    });
    this.cargarReportes();
  }

  cargarReportes(): void {
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

  /** El botón "Reportar Ahora" del navbar dispara este evento al llegar a /usuario. */
  @HostListener('window:open-report-modal')
  abrirNuevoReporte(): void {
    void this.router.navigate(['/usuario/nuevo-reporte']);
  }
}
