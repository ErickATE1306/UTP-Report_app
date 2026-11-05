import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, throwError } from 'rxjs';
import { ApiConfiguration } from '../../api/api-configuration';
import { EstadoZona, Perfil, Sede, TipoIncidente, Zona } from '../domain/models/catalogo.model';
import { NuevoReporte, Reporte } from '../domain/models/reporte.model';
import { CatalogoRepository, PerfilRepository, ReporteRepository, ZonaRepository } from '../domain/ports/usuario.ports';
import { ReporteApiDto, aReporte } from './reporte.mapper';

interface ZonaApiDto {
  id: number;
  nombre: string;
  descripcion?: string | null;
  sedeId?: number | null;
  estado?: EstadoZona | null;
}

function aZona(dto: ZonaApiDto): Zona {
  return {
    id: dto.id,
    nombre: dto.nombre,
    descripcion: dto.descripcion ?? '',
    sedeId: dto.sedeId ?? null,
    estado: dto.estado ?? null,
  };
}

@Injectable()
export class HttpReporteRepository extends ReporteRepository {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(ApiConfiguration).rootUrl}/api/reportes`;

  listarPorUsuario(username: string): Observable<Reporte[]> {
    return this.http
      .get<ReporteApiDto[]>(`${this.url}/by-username/${encodeURIComponent(username)}`)
      .pipe(
        map((reportes) => reportes.map(aReporte)),
        // El backend responde 404 cuando el usuario todavía no tiene reportes: no es un fallo.
        catchError((error: unknown) =>
          error instanceof HttpErrorResponse && error.status === 404 ? of([]) : throwError(() => error)
        )
      );
  }

  obtenerPorId(id: number): Observable<Reporte> {
    return this.http.get<ReporteApiDto>(`${this.url}/${id}`).pipe(map(aReporte));
  }

  crear(reporte: NuevoReporte, usuarioId: number): Observable<Reporte> {
    const datos = new FormData();
    datos.append('tipoIncidenteId', String(reporte.tipoIncidenteId));
    datos.append('zonaId', String(reporte.zonaId));
    datos.append('descripcion', reporte.descripcion);
    datos.append('isAnonimo', String(reporte.anonimo));
    datos.append('usuarioId', String(usuarioId));
    if (reporte.foto) datos.append('foto', reporte.foto);
    return this.http.post<ReporteApiDto>(this.url, datos).pipe(map(aReporte));
  }
}

@Injectable()
export class HttpZonaRepository extends ZonaRepository {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(ApiConfiguration).rootUrl}/api/zonas`;

  listarTodas(): Observable<Zona[]> {
    return this.http
      .get<ZonaApiDto[]>(this.url, { params: { includeInactive: true } })
      .pipe(map((zonas) => zonas.map(aZona)));
  }

  listarPorSede(sedeId: number): Observable<Zona[]> {
    return this.http.get<ZonaApiDto[]>(`${this.url}/sede/${sedeId}`).pipe(map((zonas) => zonas.map(aZona)));
  }
}

@Injectable()
export class HttpCatalogoRepository extends CatalogoRepository {
  private readonly http = inject(HttpClient);
  private readonly raiz = inject(ApiConfiguration).rootUrl;

  listarSedes(): Observable<Sede[]> {
    return this.http.get<Sede[]>(`${this.raiz}/api/sedes`);
  }

  listarTiposIncidente(): Observable<TipoIncidente[]> {
    return this.http.get<TipoIncidente[]>(`${this.raiz}/api/tipoincidentes`);
  }
}

@Injectable()
export class HttpPerfilRepository extends PerfilRepository {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(ApiConfiguration).rootUrl}/api/usuarios/me`;

  obtener(): Observable<Perfil> {
    return this.http.get<Perfil>(this.url);
  }
}
