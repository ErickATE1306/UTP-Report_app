import { Injectable } from '@angular/core';
import { ReporteControllerService } from '../../api/services/reporte-controller.service';
import { ReporteGestionControllerService } from '../../api/services/reporte-gestion-controller.service';
import { SedeControllerService } from '../../api/services/sede-controller.service';
import { TipoIncidenteControllerService } from '../../api/services/tipo-incidente-controller.service';
import { UsuarioControllerService } from '../../api/services/usuario-controller.service';
import { ZonaControllerService } from '../../api/services/zona-controller.service';
import { ReporteDto } from '../../api/models/reporte-dto';
import { ReporteGestionDto } from '../../api/models/reporte-gestion-dto';
import { SedeResponse } from '../../api/models/sede-response';
import { TipoIncidenteResponse } from '../../api/models/tipo-incidente-response';
import { UsuarioDto } from '../../api/models/usuario-dto';
import { ZonaResponse } from '../../api/models/zona-response';

export type EstadoReporte = NonNullable<ReporteGestionDto['estado']>;
export type PrioridadReporte = NonNullable<ReporteGestionDto['prioridad']>;

/**
 * Borde de integración del módulo Admin.
 *
 * El contrato actual publica respuestas con un comodín MIME; ng-openapi-gen las recibe como
 * Blob. Este servicio las normaliza aquí, sin modificar los archivos generados.
 */
@Injectable({ providedIn: 'root' })
export class AdminApiService {
  constructor(
    private readonly reportesApi: ReporteControllerService,
    private readonly gestionApi: ReporteGestionControllerService,
    private readonly zonasApi: ZonaControllerService,
    private readonly sedesApi: SedeControllerService,
    private readonly tiposApi: TipoIncidenteControllerService,
    private readonly usuariosApi: UsuarioControllerService,
  ) {}

  async cargarDatos(): Promise<[ReporteDto[], ZonaResponse[], SedeResponse[], TipoIncidenteResponse[], UsuarioDto[]]> {
    return Promise.all([
      this.normalizar<ReporteDto[]>(this.reportesApi.getAllReportes$Response()),
      this.normalizar<ZonaResponse[]>(this.zonasApi.listarZonas$Response()),
      this.normalizar<SedeResponse[]>(this.sedesApi.listarSedes$Response()),
      this.normalizar<TipoIncidenteResponse[]>(this.tiposApi.getAllTipoIncidentes$Response()),
      this.normalizar<UsuarioDto[]>(this.usuariosApi.getUsuariosByRolSeguridad$Response()),
    ]);
  }

  async asignarReporte(reporteId: number, seguridadId: number, prioridad: PrioridadReporte): Promise<ReporteGestionDto> {
    return this.normalizar<ReporteGestionDto>(this.gestionApi.createReporteGestion$Response({
      reporteId,
      seguridadId,
      prioridad,
      estado: 'EN_PROCESO',
    }));
  }

  async resolverReporte(reporteId: number, mensajeAdmin: string): Promise<ReporteGestionDto> {
    return this.normalizar<ReporteGestionDto>(this.gestionApi.marcarComoResueltoPorAdmin$Response({ reporteId, mensajeAdmin }));
  }

  async rechazarReporte(reporteId: number, mensajeAdmin: string): Promise<ReporteGestionDto> {
    return this.normalizar<ReporteGestionDto>(this.gestionApi.rechazarPorAdmin$Response({ reporteId, mensajeAdmin }));
  }

  async crearZona(nombre: string, descripcion: string, sedeId: number): Promise<ZonaResponse> {
    return this.normalizar<ZonaResponse>(this.zonasApi.crearZona$Response({ nombre, descripcion, sedeId }));
  }

  async actualizarZona(id: number, nombre: string, descripcion: string, sedeId: number): Promise<ZonaResponse> {
    return this.normalizar<ZonaResponse>(this.zonasApi.updateZona$Response({ id, nombre, descripcion, sedeId }));
  }

  async eliminarZona(id: number): Promise<void> {
    await this.zonasApi.deleteZona({ id });
  }

  private async normalizar<T>(response: Promise<{ body: unknown }>): Promise<T> {
    const { body } = await response;
    if (body instanceof Blob) {
      return JSON.parse(await body.text()) as T;
    }
    return body as T;
  }
}
