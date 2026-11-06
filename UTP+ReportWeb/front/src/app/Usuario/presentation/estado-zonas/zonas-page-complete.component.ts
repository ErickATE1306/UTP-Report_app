import { Component, OnDestroy, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ObtenerSedesUseCase, ObtenerZonasPorSedeUseCase } from '../../application/catalogos.use-cases';
import { EstadoZona, Sede, Zona } from '../../domain/models/catalogo.model';
import { etiquetaZona, filtrarZonas, resumirZonas } from '../../domain/rules/zona.rules';
import { tonoZona } from '../shared/presentacion';

@Component({
  selector: 'app-zonas-page-complete',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './zonas-page-complete.component.html',
  styleUrls: ['../shared/usuario-ui.scss', './zonas-page-complete.component.scss'],
})
export class ZonasPageCompleteComponent implements OnInit, OnDestroy {
  private readonly obtenerSedes = inject(ObtenerSedesUseCase);
  private readonly obtenerZonas = inject(ObtenerZonasPorSedeUseCase);
  private zonasSub?: Subscription;

  /** Rol desde el que se consulta, por ejemplo "administrador". */
  readonly contextLabel = input('');
  readonly showReportButton = input(true);

  readonly sedes = signal<Sede[]>([]);
  readonly sedeId = signal<number | null>(null);
  readonly zonas = signal<Zona[]>([]);
  readonly busqueda = signal('');
  readonly cargando = signal(true);
  readonly error = signal(false);

  readonly resumen = computed(() => resumirZonas(this.zonas()));
  readonly visibles = computed(() => filtrarZonas(this.zonas(), this.busqueda()));
  readonly etiqueta = etiquetaZona;
  readonly tono = tonoZona;
  readonly leyenda: EstadoZona[] = ['ZONA_SEGURA', 'ZONA_PRECAUCION', 'ZONA_PELIGROSA'];

  ngOnInit(): void {
    this.cargarSedes();
  }

  ngOnDestroy(): void {
    this.zonasSub?.unsubscribe();
  }

  cargarSedes(): void {
    this.cargando.set(true);
    this.error.set(false);
    this.obtenerSedes.ejecutar().subscribe({
      next: (sedes) => {
        this.sedes.set(sedes);
        if (sedes.length) this.seleccionarSede(sedes[0].id);
        else this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  seleccionarSede(sedeId: number): void {
    this.sedeId.set(sedeId);
    this.cargando.set(true);
    this.error.set(false);
    this.zonas.set([]);
    // Cancela la petición anterior para que una respuesta tardía no muestre otra sede.
    this.zonasSub?.unsubscribe();
    this.zonasSub = this.obtenerZonas.ejecutar(sedeId).subscribe({
      next: (zonas) => {
        this.zonas.set(zonas);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  reintentar(): void {
    const sedeId = this.sedeId();
    if (sedeId == null) this.cargarSedes();
    else this.seleccionarSede(sedeId);
  }
}
