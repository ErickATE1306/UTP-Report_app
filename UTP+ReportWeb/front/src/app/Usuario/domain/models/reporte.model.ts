export type EstadoReporte =
  | 'PENDIENTE'
  | 'EN_PROCESO'
  | 'UBICANDO'
  | 'INVESTIGANDO'
  | 'PENDIENTE_APROBACION'
  | 'RESUELTO'
  | 'CANCELADO';

export type PrioridadReporte = 'BAJA' | 'MEDIA' | 'ALTA';

export interface Reporte {
  id: number;
  usuarioId: number;
  tipoIncidenteId: number;
  zonaId: number;
  descripcion: string;
  fotoUrl: string | null;
  fechaCreacion: Date;
  anonimo: boolean;
  estado: EstadoReporte;
  prioridad: PrioridadReporte | null;
  fechaActualizacion: Date;
  mensajeSeguridad: string | null;
  mensajeAdmin: string | null;
}

/** Reporte con los nombres que el backend solo entrega como identificadores. */
export interface ReporteDetallado extends Reporte {
  tipoNombre: string;
  zonaNombre: string;
  sedeNombre: string;
}

export interface NuevoReporte {
  tipoIncidenteId: number | null;
  zonaId: number | null;
  descripcion: string;
  anonimo: boolean;
  foto: File | null;
}

export type FiltroReporte = 'TODOS' | 'ACTIVOS' | 'RESUELTOS';

export type EstadoPaso = 'completado' | 'actual' | 'pendiente';

export interface PasoSeguimiento {
  titulo: string;
  descripcion: string;
  estado: EstadoPaso;
}
