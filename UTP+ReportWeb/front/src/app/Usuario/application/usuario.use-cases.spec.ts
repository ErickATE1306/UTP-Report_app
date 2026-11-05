import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { NotificacionesLeidasStore, PerfilRepository, ReporteRepository } from '../domain/ports/usuario.ports';
import {
  FakeLeidasStore,
  FakePerfilRepository,
  FakeReporteRepository,
  PERFIL,
  SEDES,
  TIPOS,
  proveerFakes,
  unNuevoReporte,
  unReporte,
} from '../testing/usuario.fixtures';
import { ObtenerDatosFormularioUseCase } from './catalogos.use-cases';
import { ReporteInvalidoError, ReporteNoEncontradoError } from './errores';
import { CrearReporteUseCase, ObtenerDetalleReporteUseCase, ObtenerMisReportesUseCase } from './mis-reportes.use-cases';
import { NotificacionesFacade } from './notificaciones.facade';

describe('Casos de uso de Usuario', () => {
  let reportes: FakeReporteRepository;
  let perfil: FakePerfilRepository;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: proveerFakes() });
    reportes = TestBed.inject(ReporteRepository) as FakeReporteRepository;
    perfil = TestBed.inject(PerfilRepository) as FakePerfilRepository;
  });

  describe('ObtenerMisReportesUseCase', () => {
    const ejecutar = () => firstValueFrom(TestBed.inject(ObtenerMisReportesUseCase).ejecutar());

    it('completa tipo, zona y sede con los catálogos', async () => {
      const [reporte] = await ejecutar();
      expect(reporte.tipoNombre).toBe('Robo');
      expect(reporte.zonaNombre).toBe('Biblioteca');
      expect(reporte.sedeNombre).toBe('Lima Centro');
    });

    it('consulta los reportes del usuario autenticado', async () => {
      const espia = spyOn(reportes, 'listarPorUsuario').and.callThrough();
      await ejecutar();
      expect(espia).toHaveBeenCalledOnceWith(PERFIL.username);
    });

    it('usa nombres alternativos si un catálogo no incluye el dato', async () => {
      reportes.reportes = [unReporte({ tipoIncidenteId: 99, zonaId: 99 })];
      const [reporte] = await ejecutar();
      expect(reporte.tipoNombre).toBe('Incidente');
      expect(reporte.zonaNombre).toBe('Zona no disponible');
      expect(reporte.sedeNombre).toBe('Sede no disponible');
    });

    it('ordena del más reciente al más antiguo', async () => {
      reportes.reportes = [
        unReporte({ id: 1, fechaCreacion: new Date(2026, 0, 1) }),
        unReporte({ id: 2, fechaCreacion: new Date(2026, 5, 1) }),
      ];
      expect((await ejecutar()).map(({ id }) => id)).toEqual([2, 1]);
    });

    it('propaga el error del servidor', async () => {
      perfil.perfil$ = throwError(() => new Error('401'));
      await expectAsync(ejecutar()).toBeRejectedWithError('401');
    });
  });

  describe('ObtenerDetalleReporteUseCase', () => {
    const ejecutar = (id: number) => firstValueFrom(TestBed.inject(ObtenerDetalleReporteUseCase).ejecutar(id));

    it('devuelve un reporte propio con sus nombres', async () => {
      expect((await ejecutar(1024)).zonaNombre).toBe('Biblioteca');
    });

    it('no muestra el reporte de otro usuario', async () => {
      reportes.reportes = [unReporte({ id: 5, usuarioId: PERFIL.id + 1 })];
      await expectAsync(ejecutar(5)).toBeRejectedWith(jasmine.any(ReporteNoEncontradoError));
    });
  });

  describe('CrearReporteUseCase', () => {
    const ejecutar = (datos = unNuevoReporte()) => firstValueFrom(TestBed.inject(CrearReporteUseCase).ejecutar(datos));

    it('crea el reporte con el id del perfil y la descripción sin espacios sobrantes', async () => {
      const creado = await ejecutar(unNuevoReporte({ descripcion: '  Me robaron  ' }));
      expect(creado.id).toBe(2000);
      expect(reportes.creado?.usuarioId).toBe(PERFIL.id);
      expect(reportes.creado?.reporte.descripcion).toBe('Me robaron');
    });

    it('no llama al repositorio si los datos son inválidos', async () => {
      const espiaPerfil = spyOn(perfil, 'obtener').and.callThrough();
      await expectAsync(ejecutar(unNuevoReporte({ zonaId: null }))).toBeRejectedWith(jasmine.any(ReporteInvalidoError));
      expect(reportes.creado).toBeNull();
      expect(espiaPerfil).not.toHaveBeenCalled();
    });

    it('propaga el rechazo del backend, como el límite diario', async () => {
      reportes.crear$ = throwError(() => new Error('Ha alcanzado el límite de 3 reportes por día.'));
      await expectAsync(ejecutar()).toBeRejectedWithError(/límite/);
    });
  });

  describe('ObtenerDatosFormularioUseCase', () => {
    const ejecutar = () => firstValueFrom(TestBed.inject(ObtenerDatosFormularioUseCase).ejecutar());

    it('entrega los catálogos y preselecciona la sede del perfil', async () => {
      const datos = await ejecutar();
      expect(datos.sedes).toEqual(SEDES);
      expect(datos.tipos).toEqual(TIPOS);
      expect(datos.sedeInicialId).toBe(1);
    });

    it('no preselecciona una sede si el perfil no tiene', async () => {
      perfil.perfil$ = of({ ...PERFIL, sedeNombre: null });
      expect((await ejecutar()).sedeInicialId).toBeNull();
    });
  });

  describe('NotificacionesFacade', () => {
    it('carga las notificaciones y cuenta las no leídas', () => {
      const facade = TestBed.inject(NotificacionesFacade);
      facade.cargar();
      expect(facade.items().length).toBe(1);
      expect(facade.noLeidas()).toBe(1);
      expect(facade.cargando()).toBeFalse();
    });

    it('marca todas como leídas y lo guarda', () => {
      const facade = TestBed.inject(NotificacionesFacade);
      const store = TestBed.inject(NotificacionesLeidasStore) as FakeLeidasStore;
      facade.cargar();

      facade.marcarTodasLeidas();

      expect(facade.noLeidas()).toBe(0);
      expect(store.claves.has('1024:INVESTIGANDO')).toBeTrue();
    });

    it('respeta lo leído en una carga posterior', () => {
      const facade = TestBed.inject(NotificacionesFacade);
      facade.cargar();
      facade.marcarTodasLeidas();
      facade.cargar();
      expect(facade.noLeidas()).toBe(0);
    });

    it('informa el error de carga', () => {
      perfil.perfil$ = throwError(() => new Error('caído'));
      const facade = TestBed.inject(NotificacionesFacade);
      facade.cargar();
      expect(facade.error()).toBeTrue();
      expect(facade.cargando()).toBeFalse();
    });
  });
});
