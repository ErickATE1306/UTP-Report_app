import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { IncidenteService } from './incidente.service';
import { ReporteService } from './reporte.service';
import { SedeService } from './sede.service';
import { TimeService } from './time.service';
import { TipoIncidenteService } from './tipo-incidente.service';
import { UsuarioRolService } from './usuario-rol.service';
import { ZonaService } from './zona.service';

const API = 'http://localhost:8080/api';

describe('Servicios HTTP', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    spyOn(console, 'warn');
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  const error500 = { status: 500, statusText: 'Error' };

  describe('ReporteService', () => {
    let service: ReporteService;
    beforeEach(() => (service = TestBed.inject(ReporteService)));

    it('crearReporte envía un FormData con los campos obligatorios', () => {
      service.crearReporte({ tipoIncidenteId: 1, zonaId: 2, descripcion: 'Robo', isAnonimo: true, usuarioId: 3 }).subscribe();

      const req = httpMock.expectOne(`${API}/reportes`);
      const fd = req.request.body as FormData;
      expect(req.request.method).toBe('POST');
      expect(fd.get('tipoIncidenteId')).toBe('1');
      expect(fd.get('zonaId')).toBe('2');
      expect(fd.get('descripcion')).toBe('Robo');
      expect(fd.get('isAnonimo')).toBe('true');
      expect(fd.get('usuarioId')).toBe('3');
      expect(fd.has('contacto')).toBeFalse();
      expect(fd.has('foto')).toBeFalse();
      req.flush({});
    });

    it('crearReporte incluye contacto y foto cuando se proporcionan', () => {
      const foto = new File(['x'], 'foto.png', { type: 'image/png' });
      service.crearReporte({ tipoIncidenteId: 1, zonaId: 2, descripcion: 'Robo', isAnonimo: false, usuarioId: 3, contacto: '999', foto }).subscribe();

      const req = httpMock.expectOne(`${API}/reportes`);
      const fd = req.request.body as FormData;
      expect(fd.get('contacto')).toBe('999');
      expect((fd.get('foto') as File).name).toBe('foto.png');
      req.flush({});
    });

    it('getFiltered arma la URL según los filtros', () => {
      service.getFiltered().subscribe();
      httpMock.expectOne(`${API}/reportes/filter`).flush([]);

      service.getFiltered(5).subscribe();
      httpMock.expectOne(`${API}/reportes/filter?zonaId=5`).flush([]);

      service.getFiltered(null, 8).subscribe();
      httpMock.expectOne(`${API}/reportes/filter?sedeId=8`).flush([]);

      service.getFiltered(5, 8).subscribe();
      httpMock.expectOne(`${API}/reportes/filter?zonaId=5&sedeId=8`).flush([]);
    });

    it('exportExcel solicita un Blob', () => {
      service.exportExcel().subscribe();
      const req = httpMock.expectOne(`${API}/reportes/export/excel`);
      expect(req.request.responseType).toBe('blob');
      req.flush(new Blob());
    });

    it('updateGestion incluye seguridadId solo si existe', () => {
      service.updateGestion(1, 'UBICANDO', 'ALTA').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1?estado=UBICANDO&prioridad=ALTA`).flush({});

      service.updateGestion(1, 'UBICANDO', 'ALTA', 9).subscribe();
      const req = httpMock.expectOne(`${API}/reportes/gestion/1?estado=UBICANDO&prioridad=ALTA&seguridadId=9`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });

    it('irAZona y zonaUbicada usan PUT en la ruta correcta', () => {
      service.irAZona(1).subscribe();
      const ir = httpMock.expectOne(`${API}/reportes/gestion/1/ir-a-zona`);
      expect(ir.request.method).toBe('PUT');
      ir.flush({});

      service.zonaUbicada(1).subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/zona-ubicada`).flush({});
    });

    it('completarReporteConParte codifica el mensaje', () => {
      service.completarReporteConParte(1, 'todo ok & cerrado').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/completar?mensajeSeguridad=todo%20ok%20%26%20cerrado`).flush({});
    });

    it('rechazarPorAdmin omite el parámetro si el mensaje está vacío', () => {
      service.rechazarPorAdmin(1, '').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/rechazar-admin`).flush({});

      service.rechazarPorAdmin(1, 'falta info').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/rechazar-admin?mensajeAdmin=falta%20info`).flush({});
    });

    it('marcarResueltoPorAdmin omite el parámetro si el mensaje está vacío', () => {
      service.marcarResueltoPorAdmin(1, '').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/marcar-resuelto-admin`).flush({});

      service.marcarResueltoPorAdmin(1, 'listo').subscribe();
      httpMock.expectOne(`${API}/reportes/gestion/1/marcar-resuelto-admin?mensajeAdmin=listo`).flush({});
    });
  });

  describe('ZonaService', () => {
    let service: ZonaService;
    beforeEach(() => (service = TestBed.inject(ZonaService)));

    it('obtenerZonas devuelve la lista del backend', () => {
      let zonas: any;
      service.obtenerZonas().subscribe(z => (zonas = z));
      httpMock.expectOne(`${API}/zonas`).flush([{ id: 1, nombre: 'Patio' }]);
      expect(zonas).toEqual([{ id: 1, nombre: 'Patio' }]);
    });

    it('obtenerZonas devuelve una lista vacía si falla', () => {
      let zonas: any;
      service.obtenerZonas().subscribe(z => (zonas = z));
      httpMock.expectOne(`${API}/zonas`).flush(null, error500);
      expect(zonas).toEqual([]);
    });

    it('crearZona y actualizarZona devuelven null si fallan', () => {
      let creada: any = 'x';
      service.crearZona(new FormData()).subscribe(r => (creada = r));
      httpMock.expectOne(`${API}/zonas`).flush(null, error500);
      expect(creada).toBeNull();

      let actualizada: any = 'x';
      service.actualizarZona(1, new FormData()).subscribe(r => (actualizada = r));
      httpMock.expectOne(`${API}/zonas/1`).flush(null, error500);
      expect(actualizada).toBeNull();
    });

    it('eliminarZona adjunta el token y expone la respuesta completa', () => {
      localStorage.setItem('auth_token', 'abc');
      let status = 0;
      service.eliminarZona(1).subscribe(r => (status = r.status));

      const req = httpMock.expectOne(`${API}/zonas/1`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
      req.flush(null, { status: 204, statusText: 'No Content' });
      expect(status).toBe(204);
    });

    it('eliminarZona no adjunta encabezado sin token', () => {
      service.eliminarZona(1).subscribe();
      const req = httpMock.expectOne(`${API}/zonas/1`);
      expect(req.request.headers.has('Authorization')).toBeFalse();
      req.flush(null, { status: 204, statusText: 'No Content' });
    });

    it('setActivoZona propaga el error para que el componente revierta', () => {
      let huboError = false;
      service.setActivoZona(1, false).subscribe({ error: () => (huboError = true) });

      const req = httpMock.expectOne(`${API}/zonas/1/activo?activo=false`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null, error500);
      expect(huboError).toBeTrue();
    });

    it('obtenerZonasPorSede excluye las inactivas por defecto', () => {
      service.obtenerZonasPorSede(3).subscribe();
      httpMock.expectOne(`${API}/zonas/sede/3`).flush([]);
    });

    it('obtenerZonasPorSedeEx puede incluir las inactivas', () => {
      service.obtenerZonasPorSedeEx(3, true).subscribe();
      httpMock.expectOne(`${API}/zonas/sede/3?includeInactive=true`).flush([]);
    });

    it('obtenerZonasPorSedeEx no llama al backend sin sede', () => {
      let zonas: any;
      service.obtenerZonasPorSedeEx(null as any, false).subscribe(z => (zonas = z));
      httpMock.expectNone(() => true);
      expect(zonas).toEqual([]);
    });

    it('obtenerZonasPorSedeEx devuelve una lista vacía si falla', () => {
      let zonas: any;
      service.obtenerZonasPorSede(3).subscribe(z => (zonas = z));
      httpMock.expectOne(`${API}/zonas/sede/3`).flush(null, error500);
      expect(zonas).toEqual([]);
    });
  });

  describe('TimeService', () => {
    let service: TimeService;
    beforeEach(() => (service = TestBed.inject(TimeService)));

    it('getServerDateISO devuelve la cadena tal como llega', () => {
      let fecha = '';
      service.getServerDateISO().subscribe(d => (fecha = d));
      httpMock.expectOne(`${API}/time/peru-date`).flush('2025-10-11');
      expect(fecha).toBe('2025-10-11');
    });

    it('getServerDateISO convierte un objeto LocalDate con ceros a la izquierda', () => {
      let fecha = '';
      service.getServerDateISO().subscribe(d => (fecha = d));
      httpMock.expectOne(`${API}/time/peru-date`).flush({ year: 2025, monthValue: 3, dayOfMonth: 7 });
      expect(fecha).toBe('2025-03-07');
    });

    it('getServerDateISO usa la fecha local ante un formato desconocido', () => {
      let fecha = '';
      service.getServerDateISO().subscribe(d => (fecha = d));
      httpMock.expectOne(`${API}/time/peru-date`).flush({ otro: true });
      expect(fecha).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });

    it('getServerDateTime devuelve un Date', () => {
      let fecha: Date | undefined;
      service.getServerDateTime().subscribe(d => (fecha = d));
      httpMock.expectOne(`${API}/time/peru-datetime`).flush('2025-10-11T10:30:00');
      expect(fecha instanceof Date).toBeTrue();
      expect(fecha!.getFullYear()).toBe(2025);
    });
  });

  describe('TipoIncidenteService e IncidenteService', () => {
    it('getAll devuelve una lista vacía si falla', () => {
      let tipos: any;
      TestBed.inject(TipoIncidenteService).getAll().subscribe(t => (tipos = t));
      httpMock.expectOne(`${API}/tipoincidentes`).flush(null, error500);
      expect(tipos).toEqual([]);
    });

    it('create, update y delete usan el método y la ruta correctos', () => {
      const service = TestBed.inject(TipoIncidenteService);

      service.create({ nombre: 'Robo' }).subscribe();
      const crear = httpMock.expectOne(`${API}/tipoincidentes`);
      expect(crear.request.method).toBe('POST');
      expect(crear.request.body).toEqual({ nombre: 'Robo' });
      crear.flush({});

      service.update(1, { nombre: 'Hurto' }).subscribe();
      const actualizar = httpMock.expectOne(`${API}/tipoincidentes/1`);
      expect(actualizar.request.method).toBe('PUT');
      actualizar.flush({});

      service.delete(1).subscribe();
      const eliminar = httpMock.expectOne(`${API}/tipoincidentes/1`);
      expect(eliminar.request.method).toBe('DELETE');
      eliminar.flush(null);
    });

    it('obtenerTipos devuelve una lista vacía si falla', () => {
      let tipos: any;
      TestBed.inject(IncidenteService).obtenerTipos().subscribe(t => (tipos = t));
      httpMock.expectOne(`${API}/tipoincidentes`).flush(null, error500);
      expect(tipos).toEqual([]);
    });
  });

  describe('SedeService', () => {
    it('crearSede envía el nombre en el cuerpo', () => {
      TestBed.inject(SedeService).crearSede('Lima Centro').subscribe();
      const req = httpMock.expectOne(`${API}/sedes`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ nombre: 'Lima Centro' });
      req.flush({});
    });

    it('obtenerSedes propaga el error', () => {
      let huboError = false;
      TestBed.inject(SedeService).obtenerSedes().subscribe({ error: () => (huboError = true) });
      httpMock.expectOne(`${API}/sedes`).flush(null, error500);
      expect(huboError).toBeTrue();
    });
  });

  describe('UsuarioRolService', () => {
    const base = { nombreCompleto: 'Ana', username: 'ana', correo: 'a@utp.pe', password: 'x', telefono: '9', sedeId: 1 };

    it('registra cada rol en su endpoint y espera texto', () => {
      const service = TestBed.inject(UsuarioRolService);

      service.registrarUsuario({ ...base, tipoUsuario: 'ALUMNO' }).subscribe();
      const usuario = httpMock.expectOne(`${API}/auth/registrarUsuario`);
      expect(usuario.request.responseType).toBe('text');
      usuario.flush('ok');

      service.registrarAdmin(base).subscribe();
      httpMock.expectOne(`${API}/auth/registrarAdmin`).flush('ok');

      service.registrarSecurity({ ...base, zonaIds: [1, 2] }).subscribe();
      const seguridad = httpMock.expectOne(`${API}/auth/registrarSeguridad`);
      expect(seguridad.request.body.zonaIds).toEqual([1, 2]);
      seguridad.flush('ok');
    });

    it('al rechazar un registro entrega el mensaje del backend ya interpretado', () => {
      const service = TestBed.inject(UsuarioRolService);
      let recibido: any;

      service.registrarAdmin(base).subscribe({ error: (e) => (recibido = e) });
      httpMock.expectOne(`${API}/auth/registrarAdmin`).flush(
        JSON.stringify({ status: 400, message: 'El nombre de usuario ya existe' }),
        { status: 400, statusText: 'Bad Request' }
      );

      expect(recibido.status).toBe(400);
      expect(recibido.error.message).toBe('El nombre de usuario ya existe');
    });

    it('conserva el error original si el cuerpo no es JSON', () => {
      const service = TestBed.inject(UsuarioRolService);
      let recibido: any;

      service.registrarAdmin(base).subscribe({ error: (e) => (recibido = e) });
      httpMock.expectOne(`${API}/auth/registrarAdmin`).flush('Bad Gateway', { status: 502, statusText: 'Bad Gateway' });

      expect(recibido.status).toBe(502);
      expect(recibido.error).toBe('Bad Gateway');
    });
  });
});
