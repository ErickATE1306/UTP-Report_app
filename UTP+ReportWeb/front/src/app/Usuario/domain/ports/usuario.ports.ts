import { Observable } from 'rxjs';
import { Perfil, Sede, TipoIncidente, Zona } from '../models/catalogo.model';
import { NuevoReporte, Reporte } from '../models/reporte.model';

/** Puertos del dominio: la infraestructura los implementa y usuario.providers.ts los enlaza. */
export abstract class ReporteRepository {
  abstract listarPorUsuario(username: string): Observable<Reporte[]>;
  abstract obtenerPorId(id: number): Observable<Reporte>;
  abstract crear(reporte: NuevoReporte, usuarioId: number): Observable<Reporte>;
}

export abstract class ZonaRepository {
  /** Incluye zonas inactivas: un reporte antiguo debe seguir mostrando el nombre de su zona. */
  abstract listarTodas(): Observable<Zona[]>;
  abstract listarPorSede(sedeId: number): Observable<Zona[]>;
}

export abstract class CatalogoRepository {
  abstract listarSedes(): Observable<Sede[]>;
  abstract listarTiposIncidente(): Observable<TipoIncidente[]>;
}

export abstract class PerfilRepository {
  abstract obtener(): Observable<Perfil>;
}

export abstract class NotificacionesLeidasStore {
  abstract leer(): Set<string>;
  abstract guardar(claves: ReadonlySet<string>): void;
}
