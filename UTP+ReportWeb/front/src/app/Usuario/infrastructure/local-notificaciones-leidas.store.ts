import { Injectable } from '@angular/core';
import { NotificacionesLeidasStore } from '../domain/ports/usuario.ports';

const CLAVE = 'utp_usuario_notificaciones_leidas';

@Injectable()
export class LocalNotificacionesLeidasStore extends NotificacionesLeidasStore {
  leer(): Set<string> {
    try {
      const guardado = JSON.parse(localStorage.getItem(CLAVE) ?? '[]');
      return new Set(Array.isArray(guardado) ? guardado.map(String) : []);
    } catch {
      return new Set();
    }
  }

  guardar(claves: ReadonlySet<string>): void {
    try {
      localStorage.setItem(CLAVE, JSON.stringify([...claves]));
    } catch {
      // Sin almacenamiento disponible, el estado de lectura solo dura esta sesión.
    }
  }
}
