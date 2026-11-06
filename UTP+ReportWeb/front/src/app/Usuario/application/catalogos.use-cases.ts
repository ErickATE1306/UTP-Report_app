import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';
import { Perfil, Sede, TipoIncidente, Zona } from '../domain/models/catalogo.model';
import { CatalogoRepository, PerfilRepository, ZonaRepository } from '../domain/ports/usuario.ports';

export interface DatosFormularioReporte {
  sedes: Sede[];
  tipos: TipoIncidente[];
  sedeInicialId: number | null;
}

@Injectable({ providedIn: 'root' })
export class ObtenerPerfilUseCase {
  private readonly perfil = inject(PerfilRepository);

  ejecutar(): Observable<Perfil> {
    return this.perfil.obtener();
  }
}

@Injectable({ providedIn: 'root' })
export class ObtenerDatosFormularioUseCase {
  private readonly perfil = inject(PerfilRepository);
  private readonly catalogos = inject(CatalogoRepository);

  ejecutar(): Observable<DatosFormularioReporte> {
    return forkJoin({
      perfil: this.perfil.obtener(),
      sedes: this.catalogos.listarSedes(),
      tipos: this.catalogos.listarTiposIncidente(),
    }).pipe(
      map(({ perfil, sedes, tipos }) => ({
        sedes,
        tipos,
        sedeInicialId: sedes.find((sede) => sede.nombre === perfil.sedeNombre)?.id ?? null,
      })),
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ObtenerSedesUseCase {
  private readonly catalogos = inject(CatalogoRepository);

  ejecutar(): Observable<Sede[]> {
    return this.catalogos.listarSedes();
  }
}

@Injectable({ providedIn: 'root' })
export class ObtenerZonasPorSedeUseCase {
  private readonly zonas = inject(ZonaRepository);

  ejecutar(sedeId: number): Observable<Zona[]> {
    return this.zonas.listarPorSede(sedeId);
  }
}
