import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { ChartDataService } from './chart-data.service';
import { ReporteDTO, ReporteService } from './reporte.service';
import { ZonaService } from './zona.service';

describe('ChartDataService', () => {
  let service: ChartDataService;
  let reportes: jasmine.SpyObj<ReporteService>;
  let zonas: jasmine.SpyObj<ZonaService>;

  const reporte = (parcial: Partial<ReporteDTO>) => parcial as ReporteDTO;

  beforeEach(() => {
    spyOn(console, 'log');
    reportes = jasmine.createSpyObj<ReporteService>('ReporteService', ['getAll']);
    zonas = jasmine.createSpyObj<ZonaService>('ZonaService', ['obtenerZonas']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ReporteService, useValue: reportes },
        { provide: ZonaService, useValue: zonas },
      ],
    });
    service = TestBed.inject(ChartDataService);
  });

  describe('getReportStatusCount', () => {
    it('agrupa por estado y prioriza el de la gestión sobre ultimoEstado', () => {
      reportes.getAll.and.returnValue(of([
        reporte({ reporteGestion: { estado: 'RESUELTO' } as any, ultimoEstado: 'PENDIENTE' }),
        reporte({ ultimoEstado: 'RESUELTO' }),
        reporte({ ultimoEstado: 'PENDIENTE' }),
      ]));

      let resultado: any;
      service.getReportStatusCount().subscribe(r => (resultado = r));

      expect(resultado).toEqual(jasmine.arrayWithExactContents([
        { status: 'RESUELTO', count: 2 },
        { status: 'PENDIENTE', count: 1 },
      ]));
    });

    it('clasifica como DESCONOCIDO un reporte sin estado', () => {
      reportes.getAll.and.returnValue(of([reporte({})]));

      let resultado: any;
      service.getReportStatusCount().subscribe(r => (resultado = r));

      expect(resultado).toEqual([{ status: 'DESCONOCIDO', count: 1 }]);
    });

    it('devuelve una lista vacía si no hay reportes', () => {
      reportes.getAll.and.returnValue(of([]));

      let resultado: any;
      service.getReportStatusCount().subscribe(r => (resultado = r));

      expect(resultado).toEqual([]);
    });
  });

  describe('getZoneCount', () => {
    it('cuenta los reportes por zona e incluye zonas sin reportes', () => {
      zonas.obtenerZonas.and.returnValue(of([{ id: 1, nombre: 'Patio' }, { id: 2, nombre: 'Biblioteca' }]));
      reportes.getAll.and.returnValue(of([reporte({ zonaId: 1 }), reporte({ zonaId: 1 })]));

      let resultado: any;
      service.getZoneCount().subscribe(r => (resultado = r));

      expect(resultado).toEqual([
        { zoneName: 'Patio', count: 2 },
        { zoneName: 'Biblioteca', count: 0 },
      ]);
    });
  });

  describe('getReportsSentCount', () => {
    it('agrupa por fecha y ordena de forma ascendente', () => {
      reportes.getAll.and.returnValue(of([
        reporte({ fechaCreacion: '2025-10-12T12:00:00Z' }),
        reporte({ fechaCreacion: '2025-10-10T12:00:00Z' }),
        reporte({ fechaCreacion: '2025-10-12T15:00:00Z' }),
      ]));

      let resultado: any;
      service.getReportsSentCount().subscribe(r => (resultado = r));

      expect(resultado).toEqual([
        { date: '2025-10-10', count: 1 },
        { date: '2025-10-12', count: 2 },
      ]);
    });
  });
});
