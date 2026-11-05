import { Provider } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Perfil, Sede, TipoIncidente, Zona } from '../domain/models/catalogo.model';
import { NuevoReporte, Reporte, ReporteDetallado } from '../domain/models/reporte.model';
import {
  CatalogoRepository,
  NotificacionesLeidasStore,
  PerfilRepository,
  ReporteRepository,
  ZonaRepository,
} from '../domain/ports/usuario.ports';

export const PERFIL: Perfil = { id: 7, username: 'ana', nombreCompleto: 'Ana Ruiz Soto', sedeNombre: 'Lima Centro' };
export const SEDES: Sede[] = [{ id: 1, nombre: 'Lima Centro' }, { id: 2, nombre: 'Arequipa' }];
export const TIPOS: TipoIncidente[] = [{ id: 3, nombre: 'Robo' }, { id: 4, nombre: 'Accidente' }];
export const ZONAS: Zona[] = [
  { id: 5, nombre: 'Biblioteca', descripcion: 'Piso 1', sedeId: 1, estado: 'ZONA_SEGURA' },
  { id: 6, nombre: 'Patio', descripcion: '', sedeId: 1, estado: 'ZONA_PRECAUCION' },
];

export function unReporte(cambios: Partial<Reporte> = {}): Reporte {
  return {
    id: 1024,
    usuarioId: PERFIL.id,
    tipoIncidenteId: 3,
    zonaId: 5,
    descripcion: 'Me robaron la mochila',
    fotoUrl: null,
    fechaCreacion: new Date(2026, 0, 10, 8, 30),
    anonimo: false,
    estado: 'INVESTIGANDO',
    prioridad: 'ALTA',
    fechaActualizacion: new Date(2026, 0, 11, 9, 0),
    mensajeSeguridad: null,
    mensajeAdmin: null,
    ...cambios,
  };
}

export function unDetallado(cambios: Partial<ReporteDetallado> = {}): ReporteDetallado {
  return { ...unReporte(), tipoNombre: 'Robo', zonaNombre: 'Biblioteca', sedeNombre: 'Lima Centro', ...cambios };
}

export function unNuevoReporte(cambios: Partial<NuevoReporte> = {}): NuevoReporte {
  return { tipoIncidenteId: 3, zonaId: 5, descripcion: 'Me robaron', anonimo: false, foto: null, ...cambios };
}

export function unaFoto(tipo = 'image/png', bytes = 10): File {
  return new File([new Uint8Array(bytes)], 'foto.png', { type: tipo });
}

export class FakeReporteRepository extends ReporteRepository {
  reportes: Reporte[] = [unReporte()];
  crear$: Observable<Reporte> = of(unReporte({ id: 2000 }));
  creado: { reporte: NuevoReporte; usuarioId: number } | null = null;
  listar$: Observable<Reporte[]> | null = null;

  listarPorUsuario(_username: string): Observable<Reporte[]> {
    return this.listar$ ?? of(this.reportes);
  }
  obtenerPorId(id: number): Observable<Reporte> {
    return of(this.reportes.find((reporte) => reporte.id === id) ?? unReporte({ id }));
  }
  crear(reporte: NuevoReporte, usuarioId: number): Observable<Reporte> {
    this.creado = { reporte, usuarioId };
    return this.crear$;
  }
}

export class FakeZonaRepository extends ZonaRepository {
  zonas$: Observable<Zona[]> = of(ZONAS);
  listarTodas(): Observable<Zona[]> {
    return of(ZONAS);
  }
  listarPorSede(_sedeId: number): Observable<Zona[]> {
    return this.zonas$;
  }
}

export class FakeCatalogoRepository extends CatalogoRepository {
  sedes$: Observable<Sede[]> = of(SEDES);
  listarSedes(): Observable<Sede[]> {
    return this.sedes$;
  }
  listarTiposIncidente(): Observable<TipoIncidente[]> {
    return of(TIPOS);
  }
}

export class FakePerfilRepository extends PerfilRepository {
  perfil$: Observable<Perfil> = of(PERFIL);
  obtener(): Observable<Perfil> {
    return this.perfil$;
  }
}

export class FakeLeidasStore extends NotificacionesLeidasStore {
  claves = new Set<string>();
  leer(): Set<string> {
    return new Set(this.claves);
  }
  guardar(claves: ReadonlySet<string>): void {
    this.claves = new Set(claves);
  }
}

export function proveerFakes(): Provider[] {
  return [
    { provide: ReporteRepository, useClass: FakeReporteRepository },
    { provide: ZonaRepository, useClass: FakeZonaRepository },
    { provide: CatalogoRepository, useClass: FakeCatalogoRepository },
    { provide: PerfilRepository, useClass: FakePerfilRepository },
    { provide: NotificacionesLeidasStore, useClass: FakeLeidasStore },
  ];
}
