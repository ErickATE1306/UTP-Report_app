import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteDto } from '../../api/models/reporte-dto';
import { SedeResponse } from '../../api/models/sede-response';
import { TipoIncidenteResponse } from '../../api/models/tipo-incidente-response';
import { UsuarioDto } from '../../api/models/usuario-dto';
import { ZonaResponse } from '../../api/models/zona-response';
import { AdminApiService, EstadoReporte, PrioridadReporte } from '../data/admin-api.service';

type VistaAdmin = 'reportes' | 'zonas';
type FiltroEstado = EstadoReporte | 'TODOS';

@Component({
  selector: 'app-inicio-admin', standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './inicio.html', styleUrl: './inicio.scss',
})
export class InicioAdmin implements OnInit {
  readonly vista = signal<VistaAdmin>('reportes');
  readonly reportes = signal<ReporteDto[]>([]); readonly zonas = signal<ZonaResponse[]>([]);
  readonly sedes = signal<SedeResponse[]>([]); readonly tipos = signal<TipoIncidenteResponse[]>([]);
  readonly seguridad = signal<UsuarioDto[]>([]); readonly cargando = signal(true);
  readonly procesandoId = signal<number | null>(null); readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  filtroEstado: FiltroEstado = 'TODOS'; filtroTexto = '';
  prioridadSeleccionada: Record<number, PrioridadReporte> = {};
  seguridadSeleccionada: Record<number, number | null> = {};
  comentario: Record<number, string> = {};
  zonaEnEdicion: number | null = null;
  zonaForm = { nombre: '', descripcion: '', sedeId: null as number | null };

  readonly reportesFiltrados = computed(() => {
    const texto = this.filtroTexto.trim().toLowerCase();
    return this.reportes().filter((reporte) => {
      const estado = reporte.reporteGestion?.estado ?? 'PENDIENTE';
      const coincideEstado = this.filtroEstado === 'TODOS' || estado === this.filtroEstado;
      const coincideTexto = !texto || [reporte.descripcion, this.nombreZona(reporte.zonaId), this.nombreTipo(reporte.tipoIncidenteId)]
        .filter((valor): valor is string => Boolean(valor)).some((valor) => valor.toLowerCase().includes(texto));
      return coincideEstado && coincideTexto;
    });
  });
  readonly resumen = computed(() => {
    const reportes = this.reportes();
    return {
      total: reportes.length,
      pendientes: reportes.filter((r) => (r.reporteGestion?.estado ?? 'PENDIENTE') === 'PENDIENTE').length,
      proceso: reportes.filter((r) => ['EN_PROCESO', 'UBICANDO', 'INVESTIGANDO'].includes(r.reporteGestion?.estado ?? '')).length,
      aprobacion: reportes.filter((r) => r.reporteGestion?.estado === 'PENDIENTE_APROBACION').length,
      resueltos: reportes.filter((r) => r.reporteGestion?.estado === 'RESUELTO').length,
    };
  });

  constructor(private readonly adminApi: AdminApiService) {}
  ngOnInit(): void { void this.recargar(); }

  async recargar(): Promise<void> {
    this.cargando.set(true); this.error.set(null);
    try {
      const [reportes, zonas, sedes, tipos, seguridad] = await this.adminApi.cargarDatos();
      this.reportes.set(reportes); this.zonas.set(zonas); this.sedes.set(sedes); this.tipos.set(tipos);
      this.seguridad.set(seguridad.filter((usuario) => usuario.enabled !== false));
      for (const reporte of reportes) if (reporte.id) {
        this.prioridadSeleccionada[reporte.id] ??= reporte.reporteGestion?.prioridad ?? 'MEDIA';
        this.seguridadSeleccionada[reporte.id] ??= reporte.seguridadAsignadoId ?? null;
        this.comentario[reporte.id] ??= reporte.mensajeAdmin ?? '';
      }
    } catch (error: unknown) { this.error.set(this.mensajeError(error, 'No se pudieron cargar los datos del panel.')); }
    finally { this.cargando.set(false); }
  }

  seleccionarVista(vista: VistaAdmin): void { this.vista.set(vista); this.error.set(null); this.exito.set(null); }
  async asignar(reporte: ReporteDto): Promise<void> {
    if (!reporte.id) return;
    const seguridadId = this.seguridadSeleccionada[reporte.id], prioridad = this.prioridadSeleccionada[reporte.id];
    if (!seguridadId || !prioridad) { this.error.set('Selecciona una prioridad y un agente de seguridad antes de asignar.'); return; }
    await this.ejecutar(reporte.id, async () => { await this.adminApi.asignarReporte(reporte.id!, seguridadId, prioridad); this.exito.set('Reporte asignado correctamente.'); });
  }
  async resolver(reporte: ReporteDto): Promise<void> {
    if (reporte.id) await this.ejecutar(reporte.id, async () => { await this.adminApi.resolverReporte(reporte.id!, this.comentario[reporte.id!] ?? ''); this.exito.set('Reporte marcado como resuelto.'); });
  }
  async rechazar(reporte: ReporteDto): Promise<void> {
    if (reporte.id) await this.ejecutar(reporte.id, async () => { await this.adminApi.rechazarReporte(reporte.id!, this.comentario[reporte.id!] ?? ''); this.exito.set('El reporte fue devuelto a investigación.'); });
  }
  editarZona(zona?: ZonaResponse): void {
    this.zonaEnEdicion = zona?.id ?? null;
    this.zonaForm = { nombre: zona?.nombre ?? '', descripcion: zona?.descripcion ?? '', sedeId: zona?.sedeId ?? this.sedes()[0]?.id ?? null };
  }
  cancelarEdicionZona(): void { this.zonaEnEdicion = null; this.zonaForm = { nombre: '', descripcion: '', sedeId: null }; }
  async guardarZona(): Promise<void> {
    const { nombre, descripcion, sedeId } = this.zonaForm;
    if (!nombre.trim() || !descripcion.trim() || !sedeId) { this.error.set('Completa nombre, descripción y sede para guardar la zona.'); return; }
    this.procesandoId.set(this.zonaEnEdicion ?? -1); this.error.set(null);
    try {
      if (this.zonaEnEdicion) await this.adminApi.actualizarZona(this.zonaEnEdicion, nombre.trim(), descripcion.trim(), sedeId);
      else await this.adminApi.crearZona(nombre.trim(), descripcion.trim(), sedeId);
      this.exito.set(this.zonaEnEdicion ? 'Zona actualizada correctamente.' : 'Zona creada correctamente.'); this.cancelarEdicionZona(); await this.recargar();
    } catch (error: unknown) { this.error.set(this.mensajeError(error, 'No se pudo guardar la zona.')); }
    finally { this.procesandoId.set(null); }
  }
  async eliminarZona(zona: ZonaResponse): Promise<void> {
    if (!zona.id || !confirm(`¿Eliminar la zona “${zona.nombre ?? 'sin nombre'}”?`)) return;
    this.procesandoId.set(zona.id); this.error.set(null);
    try { await this.adminApi.eliminarZona(zona.id); this.exito.set('Zona eliminada correctamente.'); await this.recargar(); }
    catch (error: unknown) { this.error.set(this.mensajeError(error, 'No se pudo eliminar la zona.')); }
    finally { this.procesandoId.set(null); }
  }
  nombreZona(id?: number): string { return this.zonas().find((zona) => zona.id === id)?.nombre ?? 'Zona no disponible'; }
  nombreTipo(id?: number): string { return this.tipos().find((tipo) => tipo.id === id)?.nombre ?? 'Incidente sin clasificar'; }
  nombreSede(id?: number): string { return this.sedes().find((sede) => sede.id === id)?.nombre ?? 'Sede no disponible'; }
  estadoZona(zona: ZonaResponse): string { return zona.estado?.replace('ZONA_', '').replace('_', ' ') ?? 'Sin estado'; }
  estado(reporte: ReporteDto): EstadoReporte { return reporte.reporteGestion?.estado ?? 'PENDIENTE'; }
  puedeAsignarse(reporte: ReporteDto): boolean { return this.estado(reporte) === 'PENDIENTE'; }
  puedeDecidir(reporte: ReporteDto): boolean { return this.estado(reporte) === 'PENDIENTE_APROBACION'; }
  trackById(_: number, item: { id?: number }): number | undefined { return item.id; }
  private async ejecutar(id: number, accion: () => Promise<void>): Promise<void> {
    this.procesandoId.set(id); this.error.set(null);
    try { await accion(); await this.recargar(); } catch (error: unknown) { this.error.set(this.mensajeError(error, 'No se pudo actualizar el reporte.')); }
    finally { this.procesandoId.set(null); }
  }
  private mensajeError(error: unknown, alternativa: string): string { return error instanceof Error && error.message ? error.message : alternativa; }
}
