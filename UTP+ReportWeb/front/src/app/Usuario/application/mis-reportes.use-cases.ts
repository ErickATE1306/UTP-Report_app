import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, switchMap, throwError } from 'rxjs';
import { Sede, TipoIncidente, Zona } from '../domain/models/catalogo.model';
import { NuevoReporte, Reporte, ReporteDetallado } from '../domain/models/reporte.model';
import { CatalogoRepository, PerfilRepository, ReporteRepository, ZonaRepository } from '../domain/ports/usuario.ports';
import { validarNuevoReporte } from '../domain/rules/reporte.rules';
import { ReporteInvalidoError, ReporteNoEncontradoError } from './errores';

interface Catalogos {
  zonas: Zona[];
  sedes: Sede[];
  tipos: TipoIncidente[];
}

function detallar(reporte: Reporte, { zonas, sedes, tipos }: Catalogos): ReporteDetallado {
  const zona = zonas.find((item) => item.id === reporte.zonaId);
  return {
    ...reporte,
    tipoNombre: tipos.find((item) => item.id === reporte.tipoIncidenteId)?.nombre ?? 'Incidente',
    zonaNombre: zona?.nombre ?? 'Zona no disponible',
    sedeNombre: sedes.find((item) => item.id === zona?.sedeId)?.nombre ?? 'Sede no disponible',
  };
}

@Injectable({ providedIn: 'root' })
class CatalogosDeReporte {
  private readonly zonas = inject(ZonaRepository);
  private readonly catalogos = inject(CatalogoRepository);

  obtener(): Observable<Catalogos> {
    return forkJoin({
      zonas: this.zonas.listarTodas(),
      sedes: this.catalogos.listarSedes(),
      tipos: this.catalogos.listarTiposIncidente(),
    });
  }
}

@Injectable({ providedIn: 'root' })
export class ObtenerMisReportesUseCase {
  private readonly perfil = inject(PerfilRepository);
  private readonly reportes = inject(ReporteRepository);
  private readonly catalogos = inject(CatalogosDeReporte);

  ejecutar(): Observable<ReporteDetallado[]> {
    return this.perfil.obtener().pipe(
      switchMap(({ username }) =>
        forkJoin({ reportes: this.reportes.listarPorUsuario(username), catalogos: this.catalogos.obtener() }),
      ),
      map(({ reportes, catalogos }) =>
        reportes
          .map((reporte) => detallar(reporte, catalogos))
          .sort((a, b) => b.fechaCreacion.getTime() - a.fechaCreacion.getTime()),
      ),
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ObtenerDetalleReporteUseCase {
  private readonly perfil = inject(PerfilRepository);
  private readonly reportes = inject(ReporteRepository);
  private readonly catalogos = inject(CatalogosDeReporte);

  ejecutar(id: number): Observable<ReporteDetallado> {
    return forkJoin({
      perfil: this.perfil.obtener(),
      reporte: this.reportes.obtenerPorId(id),
      catalogos: this.catalogos.obtener(),
    }).pipe(
      map(({ perfil, reporte, catalogos }) => {
        if (reporte.usuarioId !== perfil.id) throw new ReporteNoEncontradoError();
        return detallar(reporte, catalogos);
      }),
    );
  }
}

@Injectable({ providedIn: 'root' })
export class CrearReporteUseCase {
  private readonly perfil = inject(PerfilRepository);
  private readonly reportes = inject(ReporteRepository);

  ejecutar(reporte: NuevoReporte): Observable<Reporte> {
    const errores = validarNuevoReporte(reporte);
    if (Object.keys(errores).length > 0) return throwError(() => new ReporteInvalidoError(errores));
    const limpio = { ...reporte, descripcion: reporte.descripcion.trim() };
    return this.perfil.obtener().pipe(switchMap(({ id }) => this.reportes.crear(limpio, id)));
  }
}
