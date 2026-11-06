import { EstadoReporte, PrioridadReporte, Reporte } from '../domain/models/reporte.model';

export interface ReporteApiDto {
  id: number;
  usuarioId: number;
  tipoIncidenteId: number;
  zonaId: number;
  descripcion: string;
  /** byte[] de Java: Jackson lo serializa como cadena Base64. */
  foto?: string | null;
  fechaCreacion: string;
  isAnonimo?: boolean | null;
  mensajeSeguridad?: string | null;
  mensajeAdmin?: string | null;
  reporteGestion?: {
    estado?: EstadoReporte | null;
    prioridad?: PrioridadReporte | null;
    fechaActualizacion?: string | null;
  } | null;
}

function fotoAUrl(foto: string | null | undefined): string | null {
  if (!foto) return null;
  // El backend solo admite JPG y PNG; "iVBOR" es la firma de un PNG en Base64.
  return `data:image/${foto.startsWith('iVBOR') ? 'png' : 'jpeg'};base64,${foto}`;
}

export function aReporte(dto: ReporteApiDto): Reporte {
  const gestion = dto.reporteGestion;
  return {
    id: dto.id,
    usuarioId: dto.usuarioId,
    tipoIncidenteId: dto.tipoIncidenteId,
    zonaId: dto.zonaId,
    descripcion: dto.descripcion,
    fotoUrl: fotoAUrl(dto.foto),
    fechaCreacion: new Date(dto.fechaCreacion),
    anonimo: Boolean(dto.isAnonimo),
    estado: gestion?.estado ?? 'PENDIENTE',
    prioridad: gestion?.prioridad ?? null,
    fechaActualizacion: new Date(gestion?.fechaActualizacion ?? dto.fechaCreacion),
    mensajeSeguridad: dto.mensajeSeguridad ?? null,
    mensajeAdmin: dto.mensajeAdmin ?? null,
  };
}
