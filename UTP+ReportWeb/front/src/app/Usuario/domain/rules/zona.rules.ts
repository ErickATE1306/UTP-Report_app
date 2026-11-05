import { EstadoZona, ResumenZonas, Zona } from '../models/catalogo.model';

const ETIQUETAS: Record<EstadoZona, string> = {
  ZONA_SEGURA: 'Zona segura',
  ZONA_PRECAUCION: 'Zona de precaución',
  ZONA_PELIGROSA: 'Zona peligrosa',
};

export function etiquetaZona(estado: EstadoZona | null): string {
  return estado ? ETIQUETAS[estado] : 'Sin información';
}

export function resumirZonas(zonas: ReadonlyArray<Zona>): ResumenZonas {
  const contar = (estado: EstadoZona) => zonas.filter((zona) => zona.estado === estado).length;
  const seguras = contar('ZONA_SEGURA');
  const precaucion = contar('ZONA_PRECAUCION');
  const peligrosas = contar('ZONA_PELIGROSA');
  const conteo = { seguras, precaucion, peligrosas };

  if (peligrosas > 0) {
    return { ...conteo, nivel: 'peligro', titulo: 'Atención en el campus', detalle: 'Hay zonas peligrosas: evítalas y sigue las indicaciones de Seguridad.' };
  }
  if (precaucion > 0) {
    return { ...conteo, nivel: 'precaucion', titulo: 'Campus en precaución', detalle: 'Hay zonas que requieren precaución.' };
  }
  if (seguras > 0 && seguras === zonas.length) {
    return { ...conteo, nivel: 'operativo', titulo: 'Campus operativo', detalle: 'Todas las zonas se encuentran seguras.' };
  }
  return { ...conteo, nivel: 'sin-datos', titulo: 'Sin información suficiente', detalle: 'No hay datos de estado para todas las zonas de esta sede.' };
}

export function filtrarZonas(zonas: ReadonlyArray<Zona>, texto: string): Zona[] {
  const busqueda = texto.trim().toLowerCase();
  return zonas.filter((zona) => zona.nombre.toLowerCase().includes(busqueda));
}
