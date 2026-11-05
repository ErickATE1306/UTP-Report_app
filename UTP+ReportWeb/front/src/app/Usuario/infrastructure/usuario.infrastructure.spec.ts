import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideApiConfiguration } from '../../api/api-configuration';
import { CatalogoRepository, NotificacionesLeidasStore, PerfilRepository, ReporteRepository, ZonaRepository } from '../domain/ports/usuario.ports';
import { PERFIL, unNuevoReporte, unaFoto } from '../testing/usuario.fixtures';
import { ReporteApiDto, aReporte } from './reporte.mapper';
import { provideUsuarioModule } from './usuario.providers';

const API = 'http://api.test';
const DTO: ReporteApiDto = {
  id: 1, usuarioId: 7, tipoIncidenteId: 3, zonaId: 5, descripcion: 'd', fechaCreacion: '2026-01-10T08:30:00',
};

describe('reporte.mapper', () => {
  it('sin gestión, el reporte queda pendiente y usa la fecha de creación', () => {
    const reporte = aReporte(DTO);
    expect(reporte.estado).toBe('PENDIENTE');
    expect(reporte.prioridad).toBeNull();
    expect(reporte.fechaActualizacion).toEqual(new Date('2026-01-10T08:30:00'));
    expect(reporte.fotoUrl).toBeNull();
    expect(reporte.anonimo).toBeFalse();
  });

  it('toma estado, prioridad y fecha de la gestión', () => {
    const reporte = aReporte({
      ...DTO,
      isAnonimo: true,
      reporteGestion: { estado: 'RESUELTO', prioridad: 'BAJA', fechaActualizacion: '2026-02-01T10:00:00' },
    });
    expect(reporte.estado).toBe('RESUELTO');
    expect(reporte.prioridad).toBe('BAJA');
    expect(reporte.fechaActualizacion).toEqual(new Date('2026-02-01T10:00:00'));
    expect(reporte.anonimo).toBeTrue();
  });

  it('identifica la foto PNG y JPG desde el Base64', () => {
    expect(aReporte({ ...DTO, foto: 'iVBORw0KGgo=' }).fotoUrl).toBe('data:image/png;base64,iVBORw0KGgo=');
    expect(aReporte({ ...DTO, foto: '/9j/4AAQ' }).fotoUrl).toBe('data:image/jpeg;base64,/9j/4AAQ');
  });
});

describe('Adaptadores HTTP de Usuario', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApiConfiguration(API), provideUsuarioModule()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista los reportes del usuario codificando el username', () => {
    let total = -1;
    TestBed.inject(ReporteRepository).listarPorUsuario('ana/ruiz').subscribe((reportes) => (total = reportes.length));
    const peticion = http.expectOne(`${API}/api/reportes/by-username/ana%2Fruiz`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([DTO]);
    expect(total).toBe(1);
  });

  it('un usuario sin reportes (404 del backend) recibe una lista vacía', () => {
    let reportes: unknown[] | undefined;
    TestBed.inject(ReporteRepository).listarPorUsuario('ana').subscribe((r) => (reportes = r));
    http.expectOne(`${API}/api/reportes/by-username/ana`).flush({ message: 'Sin reportes' }, { status: 404, statusText: 'Not Found' });
    expect(reportes).toEqual([]);
  });

  it('otros errores al listar reportes se siguen propagando', () => {
    let estado = 0;
    TestBed.inject(ReporteRepository).listarPorUsuario('ana').subscribe({ error: (e) => (estado = e.status) });
    http.expectOne(`${API}/api/reportes/by-username/ana`).flush(null, { status: 500, statusText: 'Error' });
    expect(estado).toBe(500);
  });

  it('obtiene un reporte por id', () => {
    let id = 0;
    TestBed.inject(ReporteRepository).obtenerPorId(1).subscribe((reporte) => (id = reporte.id));
    http.expectOne(`${API}/api/reportes/1`).flush(DTO);
    expect(id).toBe(1);
  });

  it('crea un reporte como multipart con los nombres que espera ReporteController', () => {
    const foto = unaFoto();
    TestBed.inject(ReporteRepository).crear(unNuevoReporte({ anonimo: true, foto }), 7).subscribe();

    const peticion = http.expectOne(`${API}/api/reportes`);
    const cuerpo = peticion.request.body as FormData;
    expect(peticion.request.method).toBe('POST');
    expect(cuerpo.get('tipoIncidenteId')).toBe('3');
    expect(cuerpo.get('zonaId')).toBe('5');
    expect(cuerpo.get('descripcion')).toBe('Me robaron');
    expect(cuerpo.get('isAnonimo')).toBe('true');
    expect(cuerpo.get('usuarioId')).toBe('7');
    expect(cuerpo.get('foto')).toBeInstanceOf(File);
    peticion.flush(DTO);
  });

  it('no envía el campo foto si no hay evidencia', () => {
    TestBed.inject(ReporteRepository).crear(unNuevoReporte(), 7).subscribe();
    const peticion = http.expectOne(`${API}/api/reportes`);
    expect((peticion.request.body as FormData).has('foto')).toBeFalse();
    peticion.flush(DTO);
  });

  it('lista todas las zonas incluyendo inactivas y normaliza los nulos', () => {
    let zonas: unknown;
    TestBed.inject(ZonaRepository).listarTodas().subscribe((resultado) => (zonas = resultado));
    http.expectOne(`${API}/api/zonas?includeInactive=true`).flush([{ id: 1, nombre: 'Patio' }]);
    expect(zonas).toEqual([{ id: 1, nombre: 'Patio', descripcion: '', sedeId: null, estado: null }]);
  });

  it('lista las zonas de una sede', () => {
    let total = 0;
    TestBed.inject(ZonaRepository).listarPorSede(2).subscribe((zonas) => (total = zonas.length));
    http.expectOne(`${API}/api/zonas/sede/2`).flush([{ id: 1, nombre: 'Patio', estado: 'ZONA_SEGURA' }]);
    expect(total).toBe(1);
  });

  it('consulta sedes, tipos de incidente y perfil', () => {
    TestBed.inject(CatalogoRepository).listarSedes().subscribe();
    TestBed.inject(CatalogoRepository).listarTiposIncidente().subscribe();
    let perfil: unknown;
    TestBed.inject(PerfilRepository).obtener().subscribe((resultado) => (perfil = resultado));
    http.expectOne(`${API}/api/sedes`).flush([]);
    http.expectOne(`${API}/api/tipoincidentes`).flush([]);
    http.expectOne(`${API}/api/usuarios/me`).flush(PERFIL);
    expect(perfil).toEqual(PERFIL);
  });
});

describe('LocalNotificacionesLeidasStore', () => {
  const CLAVE = 'utp_usuario_notificaciones_leidas';
  let store: NotificacionesLeidasStore;

  beforeEach(() => {
    localStorage.removeItem(CLAVE);
    TestBed.configureTestingModule({ providers: [provideUsuarioModule()] });
    store = TestBed.inject(NotificacionesLeidasStore);
  });

  afterEach(() => localStorage.removeItem(CLAVE));

  it('guarda y recupera las claves', () => {
    store.guardar(new Set(['1:RESUELTO']));
    expect(store.leer()).toEqual(new Set(['1:RESUELTO']));
  });

  it('devuelve un conjunto vacío sin datos o con datos corruptos', () => {
    expect(store.leer().size).toBe(0);
    localStorage.setItem(CLAVE, '{no-json');
    expect(store.leer().size).toBe(0);
    localStorage.setItem(CLAVE, '{"a":1}');
    expect(store.leer().size).toBe(0);
  });
});
