import { FiltroNotificacion, GrupoNotificaciones, NotificacionItem } from '../models/notificacion.model';
import { ReporteDetallado } from '../models/reporte.model';
import { codigoReporte, etiquetaEstado } from './reporte.rules';

export function claveNotificacion(item: Pick<NotificacionItem, 'id' | 'estado'>): string {
  return `${item.id}:${item.estado ?? ''}`;
}

/** Una notificación vuelve a estar sin leer cuando el reporte cambia de estado. */
export function reporteANotificacion(reporte: ReporteDetallado, leidas: ReadonlySet<string>): NotificacionItem {
  const item = {
    id: reporte.id,
    title: `Tu reporte está: ${etiquetaEstado(reporte.estado).toLowerCase()}`,
    body: `Reporte ${codigoReporte(reporte.id)} · ${reporte.tipoNombre}`,
    estado: reporte.estado,
    ts: reporte.fechaActualizacion,
  };
  return { ...item, read: leidas.has(claveNotificacion(item)) };
}

export function filtrarNotificaciones(items: ReadonlyArray<NotificacionItem>, filtro: FiltroNotificacion): NotificacionItem[] {
  if (filtro === 'ZONAS') return items.filter((item) => Boolean(item.zonaNombre));
  if (filtro === 'REPORTES') return items.filter((item) => !item.zonaNombre);
  return [...items];
}

function diasDeDiferencia(fecha: Date, hoy: Date): number {
  const inicioDia = (dia: Date) => new Date(dia.getFullYear(), dia.getMonth(), dia.getDate()).getTime();
  return Math.round((inicioDia(hoy) - inicioDia(fecha)) / 86_400_000);
}

export function agruparPorDia(items: ReadonlyArray<NotificacionItem>, hoy: Date): GrupoNotificaciones[] {
  const grupos: GrupoNotificaciones[] = [
    { titulo: 'Hoy', items: [] },
    { titulo: 'Ayer', items: [] },
    { titulo: 'Anteriores', items: [] },
  ];
  const ordenados = [...items].sort((a, b) => b.ts.getTime() - a.ts.getTime());
  for (const item of ordenados) {
    const dias = diasDeDiferencia(item.ts, hoy);
    grupos[dias <= 0 ? 0 : dias === 1 ? 1 : 2].items.push(item);
  }
  return grupos.filter((grupo) => grupo.items.length > 0);
}
