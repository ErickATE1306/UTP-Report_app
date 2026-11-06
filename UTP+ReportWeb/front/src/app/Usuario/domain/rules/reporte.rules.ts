import {
  EstadoReporte,
  FiltroReporte,
  NuevoReporte,
  PasoSeguimiento,
  PrioridadReporte,
  ReporteDetallado,
} from '../models/reporte.model';

// Debe coincidir con la columna reporte.descripcion del backend: varchar(255).
export const DESCRIPCION_MAX = 255;
export const FOTO_MAX_BYTES = 10 * 1024 * 1024;
const FOTO_TIPOS = ['image/jpeg', 'image/png'];

const ETAPAS: ReadonlyArray<{ estado: EstadoReporte; titulo: string; descripcion: string }> = [
  { estado: 'PENDIENTE', titulo: 'Reporte recibido', descripcion: 'Tu reporte ha sido registrado.' },
  { estado: 'EN_PROCESO', titulo: 'Seguridad asignada', descripcion: 'Tu caso fue asignado al equipo de Seguridad.' },
  { estado: 'UBICANDO', titulo: 'Personal en camino', descripcion: 'Personal de seguridad se dirige a la zona.' },
  { estado: 'INVESTIGANDO', titulo: 'Investigación en curso', descripcion: 'Seguridad está atendiendo tu caso.' },
  { estado: 'PENDIENTE_APROBACION', titulo: 'Pendiente de aprobación', descripcion: 'Administración está revisando el resultado.' },
  { estado: 'RESUELTO', titulo: 'Resolución', descripcion: 'Te notificaremos la resolución de tu caso.' },
];

const ETIQUETAS: Record<EstadoReporte, string> = {
  PENDIENTE: 'Recibido',
  EN_PROCESO: 'Seguridad asignada',
  UBICANDO: 'Personal en camino',
  INVESTIGANDO: 'En investigación',
  PENDIENTE_APROBACION: 'Pendiente de aprobación',
  RESUELTO: 'Resuelto',
  CANCELADO: 'Cancelado',
};

const PROGRESO: Record<EstadoReporte, number> = {
  PENDIENTE: 10,
  EN_PROCESO: 30,
  UBICANDO: 50,
  INVESTIGANDO: 70,
  PENDIENTE_APROBACION: 90,
  RESUELTO: 100,
  CANCELADO: 0,
};

const PRIORIDADES: Record<PrioridadReporte, string> = { BAJA: 'Baja', MEDIA: 'Media', ALTA: 'Alta' };

export function etiquetaEstado(estado: EstadoReporte): string {
  return ETIQUETAS[estado];
}

export function etiquetaPrioridad(prioridad: PrioridadReporte | null): string {
  return prioridad ? PRIORIDADES[prioridad] : 'Por asignar';
}

export function progresoEstado(estado: EstadoReporte): number {
  return PROGRESO[estado];
}

export function codigoReporte(id: number): string {
  return `#R-${id}`;
}

export function esResuelto(estado: EstadoReporte): boolean {
  return estado === 'RESUELTO';
}

/** Un reporte cancelado ya no está activo, pero tampoco cuenta como resuelto. */
export function esActivo(estado: EstadoReporte): boolean {
  return estado !== 'RESUELTO' && estado !== 'CANCELADO';
}

export function contarReportes(reportes: ReadonlyArray<{ estado: EstadoReporte }>) {
  return {
    total: reportes.length,
    activos: reportes.filter((reporte) => esActivo(reporte.estado)).length,
    resueltos: reportes.filter((reporte) => esResuelto(reporte.estado)).length,
  };
}

export function filtrarReportes(
  reportes: ReadonlyArray<ReporteDetallado>,
  filtro: FiltroReporte,
  texto: string,
): ReporteDetallado[] {
  const busqueda = texto.trim().toLowerCase();
  return reportes.filter((reporte) => {
    const cumpleFiltro =
      filtro === 'TODOS' ||
      (filtro === 'ACTIVOS' && esActivo(reporte.estado)) ||
      (filtro === 'RESUELTOS' && esResuelto(reporte.estado));
    const contenido = `${codigoReporte(reporte.id)} ${reporte.tipoNombre} ${reporte.zonaNombre}`.toLowerCase();
    return cumpleFiltro && contenido.includes(busqueda);
  });
}

export function construirSeguimiento(estado: EstadoReporte): PasoSeguimiento[] {
  if (estado === 'CANCELADO') {
    return [
      { titulo: ETAPAS[0].titulo, descripcion: ETAPAS[0].descripcion, estado: 'completado' },
      { titulo: 'Reporte cancelado', descripcion: 'Este reporte fue cancelado y ya no está en atención.', estado: 'actual' },
    ];
  }
  const actual = ETAPAS.findIndex((etapa) => etapa.estado === estado);
  return ETAPAS.map(({ titulo, descripcion }, indice) => ({
    titulo,
    descripcion,
    estado: indice < actual || estado === 'RESUELTO' ? 'completado' : indice === actual ? 'actual' : 'pendiente',
  }));
}

export function validarFoto(foto: File | null): string | null {
  if (!foto) return null;
  if (!FOTO_TIPOS.includes(foto.type)) return 'La foto debe ser JPG o PNG.';
  if (foto.size > FOTO_MAX_BYTES) return 'La foto no puede superar los 10 MB.';
  return null;
}

export type ErroresReporte = Partial<Record<'tipoIncidenteId' | 'zonaId' | 'descripcion' | 'foto', string>>;

export function validarNuevoReporte(reporte: NuevoReporte): ErroresReporte {
  const errores: ErroresReporte = {};
  if (reporte.tipoIncidenteId == null) errores.tipoIncidenteId = 'Selecciona el tipo de incidente.';
  if (reporte.zonaId == null) errores.zonaId = 'Selecciona la zona.';
  const descripcion = reporte.descripcion.trim();
  if (!descripcion) errores.descripcion = 'Cuéntanos qué ocurrió.';
  else if (descripcion.length > DESCRIPCION_MAX) errores.descripcion = `Usa como máximo ${DESCRIPCION_MAX} caracteres.`;
  const errorFoto = validarFoto(reporte.foto);
  if (errorFoto) errores.foto = errorFoto;
  return errores;
}
