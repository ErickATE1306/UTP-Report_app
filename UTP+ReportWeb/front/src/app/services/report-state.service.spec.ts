import { TestBed } from '@angular/core/testing';
import { ReportStateService, ReportStateSnapshot } from './report-state.service';

describe('ReportStateService', () => {
  let service: ReportStateService;
  const snap = () => service.getSnapshotValue();

  beforeEach(() => {
    service = TestBed.inject(ReportStateService);
  });

  it('inicia con un estado vacío', () => {
    expect(snap().priorities).toEqual({});
    expect(snap().inProcessIds).toEqual([]);
    expect(snap().assignedSecurity).toEqual({});
  });

  it('emite un nuevo snapshot a los suscriptores en cada cambio', () => {
    const recibidos: ReportStateSnapshot[] = [];
    service.snapshot.subscribe(s => recibidos.push(s));

    service.markInProcess(1);

    expect(recibidos.length).toBe(2);
    expect(recibidos[1].inProcessIds).toEqual([1]);
  });

  it('asigna y elimina prioridades', () => {
    service.setPriority(1, 'alta');
    expect(snap().priorities[1]).toBe('alta');

    service.setPriority(1, '');
    expect(snap().priorities[1]).toBeUndefined();
  });

  it('marca y desmarca un reporte en proceso sin duplicarlo', () => {
    service.markInProcess(1);
    service.markInProcess(1);
    expect(snap().inProcessIds).toEqual([1]);

    service.unmarkInProcess(1);
    expect(snap().inProcessIds).toEqual([]);
  });

  it('guarda el reporte al marcarlo en proceso con su objeto', () => {
    const reporte = { id: 7, descripcion: 'Robo' };
    service.markInProcessWithReporte(reporte);

    expect(snap().inProcessIds).toEqual([7]);
    expect(snap().reports![7]).toEqual(reporte);
  });

  it('ignora markInProcessWithReporte sin un reporte válido', () => {
    service.markInProcessWithReporte(null);
    service.markInProcessWithReporte({ descripcion: 'sin id' });
    expect(snap().inProcessIds).toEqual([]);
  });

  it('setReporte ignora objetos sin id', () => {
    service.setReporte({ descripcion: 'sin id' });
    expect(snap().reports).toEqual({});
  });

  it('asigna y retira el personal de seguridad', () => {
    service.setAssignedSecurity(1, { id: 9 });
    expect(snap().assignedSecurity[1]).toEqual({ id: 9 });

    service.setAssignedSecurity(1, null);
    expect(snap().assignedSecurity[1]).toBeUndefined();
  });

  it('marca y desmarca un reporte resuelto', () => {
    service.markResolved(1);
    expect(snap().resolvedIds).toEqual([1]);

    service.unmarkResolved(1);
    expect(snap().resolvedIds).toEqual([]);
  });

  it('al cancelar limpia proceso, prioridad, seguridad y resuelto', () => {
    service.markInProcess(1);
    service.setPriority(1, 'media');
    service.setAssignedSecurity(1, { id: 9 });
    service.markResolved(1);

    service.markCancelled(1);

    expect(snap().cancelledIds).toEqual([1]);
    expect(snap().inProcessIds).toEqual([]);
    expect(snap().priorities[1]).toBeUndefined();
    expect(snap().assignedSecurity[1]).toBeUndefined();
    expect(snap().resolvedIds).toEqual([]);
  });

  it('markCancelled ignora un id vacío', () => {
    service.markCancelled(0);
    expect(snap().cancelledIds ?? []).toEqual([]);
  });

  it('unmarkCancelled retira el reporte de cancelados', () => {
    service.markCancelled(1);
    service.unmarkCancelled(1);
    expect(snap().cancelledIds).toEqual([]);
  });

  it('el snapshot es una copia: modificarlo no altera el estado interno', () => {
    service.setPriority(1, 'alta');
    snap().priorities[1] = 'baja';

    service.markInProcess(2);

    expect(snap().priorities[1]).toBe('alta');
  });
});
