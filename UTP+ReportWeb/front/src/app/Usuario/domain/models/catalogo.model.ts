export type EstadoZona = 'ZONA_SEGURA' | 'ZONA_PRECAUCION' | 'ZONA_PELIGROSA';

export interface Zona {
  id: number;
  nombre: string;
  descripcion: string;
  sedeId: number | null;
  /** null cuando el backend no informa un estado: nunca se presenta como zona segura. */
  estado: EstadoZona | null;
}

export interface Sede {
  id: number;
  nombre: string;
}

export interface TipoIncidente {
  id: number;
  nombre: string;
}

export interface Perfil {
  id: number;
  username: string;
  nombreCompleto: string;
  sedeNombre: string | null;
}

export type NivelCampus = 'operativo' | 'precaucion' | 'peligro' | 'sin-datos';

export interface ResumenZonas {
  seguras: number;
  precaucion: number;
  peligrosas: number;
  nivel: NivelCampus;
  titulo: string;
  detalle: string;
}
