/** Misma forma que los elementos que el navbar entrega a <app-notificaciones>. */
export interface NotificacionItem {
  id: number;
  title: string;
  body: string;
  zonaNombre?: string;
  estado?: string;
  ts: Date;
  read: boolean;
}

export type FiltroNotificacion = 'TODAS' | 'REPORTES' | 'ZONAS';

export interface GrupoNotificaciones {
  titulo: string;
  items: NotificacionItem[];
}
