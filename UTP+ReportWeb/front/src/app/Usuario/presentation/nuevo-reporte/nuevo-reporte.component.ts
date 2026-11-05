import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  ObtenerDatosFormularioUseCase,
  ObtenerZonasPorSedeUseCase,
} from '../../application/catalogos.use-cases';
import { ReporteInvalidoError } from '../../application/errores';
import { CrearReporteUseCase } from '../../application/mis-reportes.use-cases';
import { Sede, TipoIncidente, Zona } from '../../domain/models/catalogo.model';
import {
  DESCRIPCION_MAX,
  ErroresReporte,
  validarFoto,
  validarNuevoReporte,
} from '../../domain/rules/reporte.rules';
import { mensajeDeError } from '../shared/presentacion';

const CAMPOS_POR_PASO: ReadonlyArray<ReadonlyArray<keyof ErroresReporte>> = [
  ['tipoIncidenteId', 'descripcion', 'foto'],
  ['zonaId'],
];

@Component({
  selector: 'app-nuevo-reporte',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './nuevo-reporte.component.html',
  styleUrls: ['../shared/usuario-ui.scss', './nuevo-reporte.component.scss'],
})
export class NuevoReporteComponent implements OnInit, OnDestroy {
  private readonly datosFormulario = inject(ObtenerDatosFormularioUseCase);
  private readonly zonasPorSede = inject(ObtenerZonasPorSedeUseCase);
  private readonly crearReporte = inject(CrearReporteUseCase);
  private readonly router = inject(Router);
  private zonasSub?: Subscription;

  readonly pasos = ['Detalles', 'Ubicación', 'Confirmar'];
  readonly descripcionMax = DESCRIPCION_MAX;

  readonly paso = signal(0);
  readonly sedes = signal<Sede[]>([]);
  readonly tipos = signal<TipoIncidente[]>([]);
  readonly zonas = signal<Zona[]>([]);
  readonly cargando = signal(true);
  readonly errorCarga = signal<string | null>(null);
  readonly errorZonas = signal(false);
  readonly errores = signal<ErroresReporte>({});
  readonly errorEnvio = signal<string | null>(null);
  readonly enviando = signal(false);

  readonly tipoId = signal<number | null>(null);
  readonly sedeId = signal<number | null>(null);
  readonly zonaId = signal<number | null>(null);
  readonly descripcion = signal('');
  readonly anonimo = signal(false);
  readonly foto = signal<File | null>(null);

  readonly tipoNombre = computed(() => this.tipos().find((tipo) => tipo.id === this.tipoId())?.nombre ?? '');
  readonly sedeNombre = computed(() => this.sedes().find((sede) => sede.id === this.sedeId())?.nombre ?? '');
  readonly zonaNombre = computed(() => this.zonas().find((zona) => zona.id === this.zonaId())?.nombre ?? '');

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.zonasSub?.unsubscribe();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.datosFormulario.ejecutar().subscribe({
      next: ({ sedes, tipos, sedeInicialId }) => {
        this.sedes.set(sedes);
        this.tipos.set(tipos);
        this.cargando.set(false);
        if (sedeInicialId != null) this.seleccionarSede(sedeInicialId);
      },
      error: (error: unknown) => {
        this.errorCarga.set(mensajeDeError(error, 'No pudimos cargar el formulario.'));
        this.cargando.set(false);
      },
    });
  }

  seleccionarSede(sedeId: number | null): void {
    this.sedeId.set(sedeId);
    this.zonaId.set(null);
    this.zonas.set([]);
    this.errorZonas.set(false);
    // Cancela la petición anterior para que una respuesta tardía no pise la sede elegida.
    this.zonasSub?.unsubscribe();
    if (sedeId == null) return;
    this.zonasSub = this.zonasPorSede.ejecutar(sedeId).subscribe({
      next: (zonas) => this.zonas.set(zonas),
      error: () => this.errorZonas.set(true),
    });
  }

  alElegirFoto(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const foto = campo.files?.[0] ?? null;
    const error = validarFoto(foto);
    if (error) campo.value = '';
    this.foto.set(error ? null : foto);
    this.errores.update(({ foto: _anterior, ...resto }) => (error ? { ...resto, foto: error } : resto));
  }

  quitarFoto(campo: HTMLInputElement): void {
    campo.value = '';
    this.foto.set(null);
  }

  siguiente(): void {
    const errores = validarNuevoReporte(this.borrador());
    const delPaso = CAMPOS_POR_PASO[this.paso()].filter((campo) => errores[campo]);
    this.errores.set(Object.fromEntries(delPaso.map((campo) => [campo, errores[campo]])));
    if (delPaso.length === 0) this.paso.update((paso) => paso + 1);
  }

  anterior(): void {
    this.errorEnvio.set(null);
    this.paso.update((paso) => paso - 1);
  }

  enviar(): void {
    if (this.enviando()) return;
    this.enviando.set(true);
    this.errorEnvio.set(null);
    this.crearReporte.ejecutar(this.borrador()).subscribe({
      next: ({ id }) => void this.router.navigate(['/usuario/mis-reportes', id]),
      error: (error: unknown) => {
        if (error instanceof ReporteInvalidoError) this.errores.set(error.errores);
        this.errorEnvio.set(mensajeDeError(error, 'No se pudo enviar el reporte. Inténtalo de nuevo.'));
        this.enviando.set(false);
      },
    });
  }

  private borrador() {
    return {
      tipoIncidenteId: this.tipoId(),
      zonaId: this.zonaId(),
      descripcion: this.descripcion(),
      anonimo: this.anonimo(),
      foto: this.foto(),
    };
  }
}
