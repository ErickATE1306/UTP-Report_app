import { HttpErrorResponse } from '@angular/common/http';
import { Pipe, PipeTransform } from '@angular/core';
import { EstadoZona } from '../../domain/models/catalogo.model';
import { EstadoReporte } from '../../domain/models/reporte.model';

export type Tono = 'indigo' | 'ambar' | 'verde' | 'rojo' | 'gris';

export function tonoEstado(estado: EstadoReporte): Tono {
  if (estado === 'RESUELTO') return 'verde';
  if (estado === 'CANCELADO') return 'gris';
  if (estado === 'PENDIENTE_APROBACION' || estado === 'PENDIENTE') return 'ambar';
  return 'indigo';
}

export function tonoZona(estado: EstadoZona | null): Tono {
  if (estado === 'ZONA_SEGURA') return 'verde';
  if (estado === 'ZONA_PRECAUCION') return 'ambar';
  if (estado === 'ZONA_PELIGROSA') return 'rojo';
  return 'gris';
}

export function mensajeDeError(error: unknown, alternativo: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No se pudo conectar con el servidor. Inténtalo de nuevo.';
    if (error.status === 403) return 'No tienes permiso para ver esta información.';
    const mensaje = error.error?.message;
    return typeof mensaje === 'string' && mensaje ? mensaje : alternativo;
  }
  return error instanceof Error ? error.message : alternativo;
}

const FORMATOS: Record<string, Intl.DateTimeFormatOptions> = {
  fecha: { day: 'numeric', month: 'long', year: 'numeric' },
  fechaHora: { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' },
  hora: { hour: 'numeric', minute: '2-digit' },
};

/** Usa Intl para mostrar fechas en español sin registrar un locale global en la aplicación. */
@Pipe({ name: 'fecha', standalone: true })
export class FechaPipe implements PipeTransform {
  transform(valor: Date, formato: 'fecha' | 'fechaHora' | 'hora' = 'fecha'): string {
    return new Intl.DateTimeFormat('es-PE', FORMATOS[formato]).format(valor);
  }
}
