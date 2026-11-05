import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, ActivatedRouteSnapshot, Router, RouterStateSnapshot, convertToParamMap, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { NotificacionesFacade } from '../application/notificaciones.facade';
import { CatalogoRepository, PerfilRepository, ReporteRepository, ZonaRepository } from '../domain/ports/usuario.ports';
import { Zona } from '../domain/models/catalogo.model';
import { NotificacionItem } from '../domain/models/notificacion.model';
import {
  FakeCatalogoRepository,
  FakePerfilRepository,
  FakeReporteRepository,
  FakeZonaRepository,
  PERFIL,
  ZONAS,
  proveerFakes,
  unReporte,
  unaFoto,
} from '../testing/usuario.fixtures';
import { ZonasPageCompleteComponent as ZonasDesdeRutaAdmin } from '../Estado_zonas/zonas-page-complete.component';
import { Notificaciones as NotificacionesDesdeRutaNavbar } from '../notificaciones/notificaciones';
import { DetalleReporteRouteComponent } from './detalle-reporte/detalle-reporte-route.component';
import { DetalleReporteComponent } from './detalle-reporte/detalle-reporte.component';
import { ZonasPageCompleteComponent } from './estado-zonas/zonas-page-complete.component';
import { GuiaSeguridadComponent } from './guia/guia-seguridad.component';
import { InicioUsuarioComponent } from './inicio/inicio-usuario.component';
import { UsuarioLayoutComponent } from './layout/usuario-layout.component';
import { MisReportesComponent } from './mis-reportes/mis-reportes.component';
import { NotificacionesPageComponent } from './notificaciones/notificaciones-page.component';
import { NotificacionesComponent } from './notificaciones/notificaciones.component';
import { NuevoReporteComponent } from './nuevo-reporte/nuevo-reporte.component';
import { FechaPipe, mensajeDeError, tonoEstado, tonoZona } from './shared/presentacion';
import { usuarioGuard } from './usuario.guard';

@Component({ template: '', standalone: true })
class Vacio {}

function texto<T>(fixture: ComponentFixture<T>): string {
  return (fixture.nativeElement as HTMLElement).textContent ?? '';
}

function buscar<T, E extends Element = HTMLElement>(fixture: ComponentFixture<T>, selector: string): E {
  const elemento = (fixture.nativeElement as HTMLElement).querySelector<E>(selector);
  if (!elemento) throw new Error(`No se encontró ${selector}`);
  return elemento;
}

function botonConTexto<T>(fixture: ComponentFixture<T>, contenido: string): HTMLButtonElement {
  const botones = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
  const boton = botones.find((item) => item.textContent?.includes(contenido));
  if (!boton) throw new Error(`No se encontró el botón ${contenido}`);
  return boton;
}

function configurar(): void {
  TestBed.configureTestingModule({
    providers: [...proveerFakes(), provideRouter([{ path: '**', component: Vacio }])],
  });
}

describe('presentacion (utilidades)', () => {
  it('asigna un tono a cada estado', () => {
    expect(tonoEstado('RESUELTO')).toBe('verde');
    expect(tonoEstado('CANCELADO')).toBe('gris');
    expect(tonoEstado('PENDIENTE_APROBACION')).toBe('ambar');
    expect(tonoEstado('INVESTIGANDO')).toBe('indigo');
    expect(tonoZona('ZONA_PELIGROSA')).toBe('rojo');
    expect(tonoZona(null)).toBe('gris');
  });

  it('prioriza el mensaje del backend y distingue la falta de conexión', () => {
    expect(mensajeDeError(new HttpErrorResponse({ status: 400, error: { message: 'Límite' } }), 'x')).toBe('Límite');
    expect(mensajeDeError(new HttpErrorResponse({ status: 500 }), 'alternativo')).toBe('alternativo');
    expect(mensajeDeError(new HttpErrorResponse({ status: 0 }), 'x')).toContain('conectar');
    expect(mensajeDeError(new HttpErrorResponse({ status: 403, error: { message: 'Forbidden' } }), 'x')).toContain('permiso');
    expect(mensajeDeError(new Error('propio'), 'x')).toBe('propio');
    expect(mensajeDeError('raro', 'alternativo')).toBe('alternativo');
  });

  it('formatea fechas en español', () => {
    expect(new FechaPipe().transform(new Date(2026, 0, 10))).toContain('enero');
  });
});

describe('usuarioGuard', () => {
  let roles: string[];
  let autenticado: boolean;

  const evaluar = () =>
    TestBed.runInInjectionContext(() => usuarioGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  beforeEach(() => {
    roles = ['ROLE_USUARIO'];
    autenticado = true;
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: () => autenticado, hasRole: (rol: string) => roles.includes(rol) } },
      ],
    });
  });

  it('permite a un usuario con sesión vigente', () => {
    expect(evaluar()).toBeTrue();
  });

  it('permite a un administrador que actúa como usuario', () => {
    roles = ['ROLE_ADMIN'];
    expect(evaluar()).toBeTrue();
  });

  it('rechaza otros roles', () => {
    roles = ['ROLE_SEGURIDAD'];
    expect(String(evaluar())).toBe('/login');
  });

  it('rechaza sin sesión vigente, aunque queden roles guardados', () => {
    autenticado = false;
    expect(String(evaluar())).toBe('/login');
  });
});

describe('Rutas de compatibilidad', () => {
  it('Admin y el navbar reciben los componentes de la capa de presentación', () => {
    expect(ZonasDesdeRutaAdmin).toBe(ZonasPageCompleteComponent);
    expect(NotificacionesDesdeRutaNavbar).toBe(NotificacionesComponent);
  });
});

describe('InicioUsuarioComponent', () => {
  let reportes: FakeReporteRepository;

  beforeEach(() => {
    configurar();
    reportes = TestBed.inject(ReporteRepository) as FakeReporteRepository;
  });

  it('saluda por el primer nombre y muestra los contadores', () => {
    reportes.reportes = [unReporte({ id: 1 }), unReporte({ id: 2, estado: 'RESUELTO' })];
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();

    expect(buscar(fixture, 'h1').textContent).toContain('Hola, Ana');
    expect(fixture.componentInstance.conteo()).toEqual({ total: 2, activos: 1, resueltos: 1 });
  });

  it('destaca como máximo dos reportes, priorizando los activos', () => {
    reportes.reportes = [
      unReporte({ id: 1, estado: 'RESUELTO', fechaCreacion: new Date(2026, 5, 1) }),
      unReporte({ id: 2, fechaCreacion: new Date(2026, 4, 1) }),
      unReporte({ id: 3, fechaCreacion: new Date(2026, 3, 1) }),
      unReporte({ id: 4, fechaCreacion: new Date(2026, 2, 1) }),
    ];
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.destacados().map(({ id }) => id)).toEqual([2, 3]);
  });

  it('sin reportes activos, muestra los más recientes', () => {
    reportes.reportes = [unReporte({ id: 1, estado: 'RESUELTO' })];
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.destacados().map(({ id }) => id)).toEqual([1]);
  });

  it('muestra un estado vacío', () => {
    reportes.reportes = [];
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Aún no has enviado reportes');
  });

  it('permite reintentar tras un error', () => {
    reportes.listar$ = throwError(() => new Error('caído'));
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(texto(fixture)).toContain('No pudimos cargar');

    reportes.listar$ = of([unReporte()]);
    botonConTexto(fixture, 'Reintentar').click();
    fixture.detectChanges();

    expect(texto(fixture)).toContain('#R-1024');
  });

  it('sigue saludando si falla el perfil', () => {
    (TestBed.inject(PerfilRepository) as FakePerfilRepository).perfil$ = throwError(() => new Error('caído'));
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(buscar(fixture, 'h1').textContent?.trim()).toBe('Hola');
  });

  it('atiende el evento del botón "Reportar Ahora" del navbar', () => {
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    const navegar = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);

    window.dispatchEvent(new Event('open-report-modal'));

    expect(navegar).toHaveBeenCalledOnceWith(['/usuario/nuevo-reporte']);
  });

  it('conserva el botón que busca el tour del sistema', () => {
    const fixture = TestBed.createComponent(InicioUsuarioComponent);
    fixture.detectChanges();
    expect(buscar(fixture, '.quick-actions-section button[aria-label="Ver mis reportes anteriores"]')).toBeTruthy();
  });
});

describe('NuevoReporteComponent', () => {
  let fixture: ComponentFixture<NuevoReporteComponent>;
  let componente: NuevoReporteComponent;
  let reportes: FakeReporteRepository;
  let zonas: FakeZonaRepository;

  function completarDetalles(): void {
    componente.tipoId.set(3);
    componente.descripcion.set('Me robaron');
  }

  beforeEach(() => {
    configurar();
    reportes = TestBed.inject(ReporteRepository) as FakeReporteRepository;
    zonas = TestBed.inject(ZonaRepository) as FakeZonaRepository;
    fixture = TestBed.createComponent(NuevoReporteComponent);
    componente = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('preselecciona la sede del perfil y carga sus zonas', () => {
    expect(componente.sedeId()).toBe(1);
    expect(componente.zonas()).toEqual(ZONAS);
  });

  it('no avanza con datos incompletos y muestra los errores', () => {
    botonConTexto(fixture, 'Continuar').click();
    fixture.detectChanges();

    expect(componente.paso()).toBe(0);
    expect(componente.errores().tipoIncidenteId).toBeDefined();
    expect(componente.errores().descripcion).toBeDefined();
    expect(texto(fixture)).toContain('Cuéntanos qué ocurrió');
  });

  it('el primer paso no exige la zona, que se elige en el segundo', () => {
    completarDetalles();
    componente.siguiente();
    expect(componente.paso()).toBe(1);
    expect(componente.errores()).toEqual({});
  });

  it('el segundo paso exige la zona', () => {
    completarDetalles();
    componente.siguiente();
    componente.siguiente();
    expect(componente.paso()).toBe(1);
    expect(componente.errores().zonaId).toBeDefined();
  });

  it('cambiar de sede limpia la zona elegida', () => {
    componente.zonaId.set(5);
    componente.seleccionarSede(2);
    expect(componente.zonaId()).toBeNull();
  });

  it('ignora una respuesta tardía de la sede anterior', () => {
    const lenta = new Subject<Zona[]>();
    zonas.zonas$ = lenta;
    componente.seleccionarSede(2);
    zonas.zonas$ = of([ZONAS[1]]);
    componente.seleccionarSede(1);

    lenta.next([ZONAS[0]]);

    expect(componente.zonas()).toEqual([ZONAS[1]]);
  });

  it('sin sede no consulta zonas', () => {
    const espia = spyOn(zonas, 'listarPorSede').and.callThrough();
    componente.seleccionarSede(null);
    expect(espia).not.toHaveBeenCalled();
    expect(componente.zonas()).toEqual([]);
  });

  it('informa si fallan las zonas', () => {
    zonas.zonas$ = throwError(() => new Error('caído'));
    componente.seleccionarSede(2);
    expect(componente.errorZonas()).toBeTrue();
  });

  it('envía el reporte y abre su detalle', () => {
    const navegar = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    completarDetalles();
    componente.anonimo.set(true);
    componente.siguiente();
    componente.zonaId.set(5);
    componente.siguiente();
    fixture.detectChanges();

    expect(texto(fixture)).toContain('Revisa tu reporte');
    expect(texto(fixture)).toContain('Biblioteca');
    botonConTexto(fixture, 'Enviar reporte').click();

    expect(reportes.creado?.usuarioId).toBe(PERFIL.id);
    expect(reportes.creado?.reporte).toEqual(jasmine.objectContaining({ tipoIncidenteId: 3, zonaId: 5, anonimo: true }));
    expect(navegar).toHaveBeenCalledOnceWith(['/usuario/mis-reportes', 2000]);
  });

  it('no envía dos veces mientras hay un envío en curso', () => {
    const pendiente = new Subject<never>();
    reportes.crear$ = pendiente;
    const espia = spyOn(reportes, 'crear').and.callThrough();
    completarDetalles();
    componente.zonaId.set(5);

    componente.enviar();
    componente.enviar();

    expect(espia).toHaveBeenCalledTimes(1);
    expect(componente.enviando()).toBeTrue();
  });

  it('muestra el límite diario del backend y permite reintentar', () => {
    reportes.crear$ = throwError(() => new HttpErrorResponse({ status: 500, error: { message: 'Ha alcanzado el límite de 3 reportes por día.' } }));
    completarDetalles();
    componente.zonaId.set(5);
    componente.paso.set(2);

    componente.enviar();
    fixture.detectChanges();

    expect(texto(fixture)).toContain('límite de 3 reportes');
    expect(componente.enviando()).toBeFalse();
  });

  it('expone los errores de validación al enviar datos inválidos', () => {
    componente.enviar();
    expect(componente.errores().zonaId).toBeDefined();
    expect(componente.enviando()).toBeFalse();
  });

  describe('foto', () => {
    function elegir(foto: File | null): { value: string; files: File[] } {
      const campo = { value: 'C:\\foto', files: foto ? [foto] : [] };
      componente.alElegirFoto({ target: campo } as unknown as Event);
      return campo;
    }

    it('acepta una foto válida', () => {
      const foto = unaFoto();
      elegir(foto);
      expect(componente.foto()).toBe(foto);
      expect(componente.errores().foto).toBeUndefined();
    });

    it('rechaza una foto inválida y limpia el campo', () => {
      const campo = elegir(unaFoto('application/pdf'));
      expect(componente.foto()).toBeNull();
      expect(campo.value).toBe('');
      expect(componente.errores().foto).toContain('JPG o PNG');
    });

    it('una foto válida borra el error anterior', () => {
      elegir(unaFoto('application/pdf'));
      elegir(unaFoto());
      expect(componente.errores().foto).toBeUndefined();
    });

    it('permite quitar la foto', () => {
      elegir(unaFoto());
      const campo = { value: 'C:\\foto' } as HTMLInputElement;
      componente.quitarFoto(campo);
      expect(componente.foto()).toBeNull();
      expect(campo.value).toBe('');
    });
  });

  it('permite volver al paso anterior', () => {
    completarDetalles();
    componente.siguiente();
    componente.anterior();
    expect(componente.paso()).toBe(0);
  });

  it('permite reintentar si falla la carga del formulario', () => {
    const catalogo = TestBed.inject(CatalogoRepository) as FakeCatalogoRepository;
    catalogo.sedes$ = throwError(() => new HttpErrorResponse({ status: 0 }));
    componente.cargarDatos();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('No se pudo conectar');

    catalogo.sedes$ = of([]);
    botonConTexto(fixture, 'Reintentar').click();
    expect(componente.errorCarga()).toBeNull();
  });
});

describe('MisReportesComponent', () => {
  let fixture: ComponentFixture<MisReportesComponent>;
  let reportes: FakeReporteRepository;

  beforeEach(() => {
    configurar();
    reportes = TestBed.inject(ReporteRepository) as FakeReporteRepository;
    reportes.reportes = [unReporte({ id: 1 }), unReporte({ id: 2, estado: 'RESUELTO' }), unReporte({ id: 3, estado: 'CANCELADO' })];
    fixture = TestBed.createComponent(MisReportesComponent);
    fixture.detectChanges();
  });

  it('muestra los contadores y todos los reportes', () => {
    expect(fixture.componentInstance.opciones().map(({ cantidad }) => cantidad)).toEqual([3, 1, 1]);
    expect(fixture.nativeElement.querySelectorAll('app-reporte-card').length).toBe(3);
  });

  it('filtra con los botones', () => {
    botonConTexto(fixture, 'Resueltos').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('app-reporte-card').length).toBe(1);
    expect(texto(fixture)).toContain('#R-2');
    expect(botonConTexto(fixture, 'Resueltos').getAttribute('aria-pressed')).toBe('true');
  });

  it('distingue "sin coincidencias" de "sin reportes"', () => {
    fixture.componentInstance.busqueda.set('zzz');
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Ningún reporte coincide');

    reportes.reportes = [];
    fixture.componentInstance.busqueda.set('');
    fixture.componentInstance.cargar();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Aún no has enviado reportes');
  });

  it('muestra el error de carga', () => {
    reportes.listar$ = throwError(() => new Error('caído'));
    fixture.componentInstance.cargar();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('No pudimos cargar');
  });

  it('cada tarjeta enlaza con el detalle', () => {
    expect(buscar<MisReportesComponent, HTMLAnchorElement>(fixture, 'app-reporte-card a').getAttribute('href')).toBe('/usuario/mis-reportes/1');
  });
});

describe('DetalleReporteComponent', () => {
  function crear(id = '1024') {
    const fixture = TestBed.createComponent(DetalleReporteComponent);
    fixture.componentRef.setInput('id', id);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(configurar);

  it('muestra el estado, el avance, los datos y el seguimiento', () => {
    const fixture = crear();
    expect(texto(fixture)).toContain('#R-1024');
    expect(texto(fixture)).toContain('En investigación');
    expect(texto(fixture)).toContain('70%');
    expect(texto(fixture)).toContain('Biblioteca');
    expect(texto(fixture)).toContain('Lima Centro');
    expect(texto(fixture)).toContain('Alta');
    expect(fixture.nativeElement.querySelectorAll('.seguimiento li').length).toBe(6);
    expect(buscar(fixture, '.seguimiento li[aria-current="step"]').textContent).toContain('Investigación en curso');
  });

  it('muestra foto y mensajes solo si existen', () => {
    const repositorio = TestBed.inject(ReporteRepository) as FakeReporteRepository;
    expect(crear().nativeElement.querySelector('img')).toBeNull();

    repositorio.reportes = [unReporte({ fotoUrl: 'data:image/png;base64,AA==', mensajeSeguridad: 'Revisado', mensajeAdmin: 'Cerrado', anonimo: true })];
    const fixture = crear();

    expect(buscar<DetalleReporteComponent, HTMLImageElement>(fixture, 'img').alt).toContain('Evidencia');
    expect(texto(fixture)).toContain('Revisado');
    expect(texto(fixture)).toContain('Cerrado');
    expect(texto(fixture)).toContain('anónimo');
  });

  it('indica la prioridad pendiente de asignar', () => {
    (TestBed.inject(ReporteRepository) as FakeReporteRepository).reportes = [unReporte({ prioridad: null })];
    expect(texto(crear())).toContain('Por asignar');
  });

  it('no muestra el reporte de otro usuario', () => {
    (TestBed.inject(ReporteRepository) as FakeReporteRepository).reportes = [unReporte({ usuarioId: 999 })];
    const fixture = crear();
    expect(texto(fixture)).toContain('No encontramos este reporte');
    expect(texto(fixture)).not.toContain('Me robaron la mochila');
  });

  it('solo ofrece reintentar si el error puede resolverse al repetir', () => {
    const repositorio = TestBed.inject(ReporteRepository);
    const buscarPorId = spyOn(repositorio, 'obtenerPorId').and.returnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    const fixture = crear();
    expect(texto(fixture)).toContain('No tienes permiso');
    expect(texto(fixture)).not.toContain('Reintentar');

    buscarPorId.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.componentInstance.cargar();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Reintentar');
  });

  it('el componente de ruta entrega el parámetro id', () => {
    TestBed.overrideProvider(ActivatedRoute, { useValue: { snapshot: { paramMap: convertToParamMap({ id: '1024' }) } } });
    const fixture = TestBed.createComponent(DetalleReporteRouteComponent);
    fixture.detectChanges();
    expect(texto(fixture)).toContain('#R-1024');
    expect(texto(fixture)).toContain('Me robaron la mochila');
  });
});

describe('ZonasPageCompleteComponent', () => {
  let zonas: FakeZonaRepository;
  let catalogo: FakeCatalogoRepository;

  function crear(entradas: Record<string, unknown> = {}) {
    const fixture = TestBed.createComponent(ZonasPageCompleteComponent);
    Object.entries(entradas).forEach(([nombre, valor]) => fixture.componentRef.setInput(nombre, valor));
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(() => {
    configurar();
    zonas = TestBed.inject(ZonaRepository) as FakeZonaRepository;
    catalogo = TestBed.inject(CatalogoRepository) as FakeCatalogoRepository;
  });

  it('carga la primera sede, el resumen y las zonas', () => {
    const fixture = crear();
    expect(fixture.componentInstance.sedeId()).toBe(1);
    expect(texto(fixture)).toContain('Campus en precaución');
    expect(texto(fixture)).toContain('Biblioteca');
    expect(texto(fixture)).toContain('Zona de precaución');
  });

  it('solo muestra el escudo con check si todas las zonas son seguras', () => {
    expect(buscar(crear(), '.campus > i').classList).toContain('bi-shield-exclamation');

    zonas.zonas$ = of([ZONAS[0]]);
    expect(buscar(crear(), '.campus > i').classList).toContain('bi-shield-check');
  });

  it('acepta las entradas que usa la vista de administrador', () => {
    const fixture = crear({ contextLabel: 'administrador', showReportButton: false });
    expect(texto(fixture)).toContain('Vista de administrador');
    expect(fixture.nativeElement.querySelector('a.u-btn')).toBeNull();
  });

  it('muestra el botón de reportar por defecto', () => {
    expect(buscar<ZonasPageCompleteComponent, HTMLAnchorElement>(crear(), 'a.u-btn').getAttribute('href')).toBe('/usuario/nuevo-reporte');
  });

  it('busca zonas', () => {
    const fixture = crear();
    fixture.componentInstance.busqueda.set('patio');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('li.zona').length).toBe(1);

    fixture.componentInstance.busqueda.set('zzz');
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Ninguna zona coincide');
  });

  it('muestra una sede sin zonas', () => {
    zonas.zonas$ = of([]);
    expect(texto(crear())).toContain('No hay zonas registradas');
  });

  it('no queda cargando si no hay sedes', () => {
    catalogo.sedes$ = of([]);
    expect(crear().componentInstance.cargando()).toBeFalse();
  });

  it('ignora la respuesta tardía de otra sede', () => {
    const fixture = crear();
    const lenta = new Subject<Zona[]>();
    zonas.zonas$ = lenta;
    fixture.componentInstance.seleccionarSede(2);
    zonas.zonas$ = of([ZONAS[1]]);
    fixture.componentInstance.seleccionarSede(1);

    lenta.next([ZONAS[0]]);

    expect(fixture.componentInstance.zonas()).toEqual([ZONAS[1]]);
  });

  it('reintenta las zonas si falla una sede', () => {
    zonas.zonas$ = throwError(() => new Error('caído'));
    const fixture = crear();
    expect(texto(fixture)).toContain('No pudimos cargar');

    zonas.zonas$ = of(ZONAS);
    botonConTexto(fixture, 'Reintentar').click();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('Biblioteca');
  });

  it('reintenta las sedes si fallaron', () => {
    catalogo.sedes$ = throwError(() => new Error('caído'));
    const fixture = crear();
    expect(fixture.componentInstance.error()).toBeTrue();

    catalogo.sedes$ = of([{ id: 1, nombre: 'Lima' }]);
    fixture.componentInstance.reintentar();
    expect(fixture.componentInstance.sedeId()).toBe(1);
  });
});

describe('NotificacionesComponent', () => {
  const ahora = new Date();
  const items: NotificacionItem[] = [
    { id: 1, title: 'Reporte', body: 'R', ts: ahora, read: false },
    { id: 2, title: 'Zona', body: 'Z', zonaNombre: 'Patio', ts: ahora, read: true },
  ];

  function crear(entradas: Record<string, unknown>) {
    const fixture = TestBed.createComponent(NotificacionesComponent);
    Object.entries(entradas).forEach(([nombre, valor]) => fixture.componentRef.setInput(nombre, valor));
    fixture.detectChanges();
    return fixture;
  }

  it('acepta los elementos del navbar y los agrupa', () => {
    const fixture = crear({ items });
    expect(texto(fixture)).toContain('Hoy');
    expect(fixture.nativeElement.querySelectorAll('.item').length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('.punto').length).toBe(1);
  });

  it('filtra por tipo', () => {
    const fixture = crear({ items });
    botonConTexto(fixture, 'Zonas').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.item').length).toBe(1);
    expect(buscar(fixture, '.item strong').textContent).toBe('Zona');
  });

  it('muestra un estado vacío', () => {
    expect(texto(crear({ items: [] }))).toContain('No tienes notificaciones');
  });

  it('en el panel del navbar no ofrece "marcar todo"', () => {
    expect(texto(crear({ items }))).not.toContain('Marcar todo');
  });

  it('en la página emite "marcar todo" si hay no leídas', () => {
    const fixture = crear({ items, permitirMarcar: true });
    const emitido = jasmine.createSpy();
    fixture.componentInstance.marcarTodas.subscribe(emitido);
    botonConTexto(fixture, 'Marcar todo').click();
    expect(emitido).toHaveBeenCalledTimes(1);
  });

  it('oculta "marcar todo" si todo está leído', () => {
    const fixture = crear({ items: [items[1]], permitirMarcar: true });
    expect(texto(fixture)).not.toContain('Marcar todo');
  });
});

describe('NotificacionesPageComponent', () => {
  beforeEach(configurar);

  it('muestra las notificaciones y permite marcarlas como leídas', () => {
    const fixture = TestBed.createComponent(NotificacionesPageComponent);
    fixture.detectChanges();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('#R-1024');

    botonConTexto(fixture, 'Marcar todo').click();
    fixture.detectChanges();

    expect(fixture.componentInstance.facade.noLeidas()).toBe(0);
    expect(texto(fixture)).not.toContain('Marcar todo');
  });

  it('permite reintentar tras un error', () => {
    const perfil = TestBed.inject(PerfilRepository) as FakePerfilRepository;
    perfil.perfil$ = throwError(() => new Error('caído'));
    const fixture = TestBed.createComponent(NotificacionesPageComponent);
    fixture.detectChanges();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('No pudimos cargar');

    perfil.perfil$ = of(PERFIL);
    botonConTexto(fixture, 'Reintentar').click();
    fixture.detectChanges();
    expect(texto(fixture)).toContain('#R-1024');
  });

  it('recarga al abrirse para incluir los cambios recientes', () => {
    const reportes = TestBed.inject(ReporteRepository) as FakeReporteRepository;
    const facade = TestBed.inject(NotificacionesFacade);
    facade.cargar();
    reportes.reportes = [unReporte(), unReporte({ id: 2000 })];

    TestBed.createComponent(NotificacionesPageComponent).detectChanges();

    expect(facade.items().length).toBe(2);
  });
});

describe('GuiaSeguridadComponent', () => {
  beforeEach(configurar);

  it('presenta los cinco temas y la acción de emergencia', () => {
    const fixture = TestBed.createComponent(GuiaSeguridadComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('details').length).toBe(5);
    expect(texto(fixture)).toContain('En caso de emergencia');
    expect(buscar<GuiaSeguridadComponent, HTMLAnchorElement>(fixture, 'a.u-btn').getAttribute('href')).toBe('/usuario/nuevo-reporte');
  });
});

describe('UsuarioLayoutComponent', () => {
  const logout = jasmine.createSpy('logout');

  beforeEach(() => {
    logout.calls.reset();
    TestBed.configureTestingModule({
      providers: [
        ...proveerFakes(),
        provideRouter([{ path: '**', component: Vacio }]),
        { provide: AuthService, useValue: { logout, roles: signal([]) } },
      ],
    });
  });

  it('marca las notificaciones sin leer en la campana', () => {
    const fixture = TestBed.createComponent(UsuarioLayoutComponent);
    fixture.detectChanges();
    expect(buscar(fixture, '.campana').getAttribute('aria-label')).toBe('Notificaciones, 1 sin leer');
    expect(fixture.nativeElement.querySelector('.punto')).not.toBeNull();
  });

  it('oculta "Volver" en el inicio y lo muestra en otras secciones', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/usuario');
    const fixture = TestBed.createComponent(UsuarioLayoutComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[aria-label="Volver"]')).toBeNull();

    await router.navigateByUrl('/usuario/mis-reportes');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[aria-label="Volver"]')).not.toBeNull();
  });

  it('cierra la sesión y vuelve al login', () => {
    const navegar = spyOn(TestBed.inject(Router), 'navigateByUrl').and.resolveTo(true);
    const fixture = TestBed.createComponent(UsuarioLayoutComponent);
    fixture.detectChanges();

    buscar(fixture, '[aria-label="Cerrar sesión"]').click();

    expect(logout).toHaveBeenCalledTimes(1);
    expect(navegar).toHaveBeenCalledOnceWith('/login');
  });
});
