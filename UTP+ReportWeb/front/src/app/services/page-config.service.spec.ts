import { TestBed } from '@angular/core/testing';
import { PageConfigService } from './page-config.service';
import { ROLES } from '../constants/roles';

describe('PageConfigService', () => {
  const STORAGE_KEY = 'page_config_v1';
  const crear = () => TestBed.inject(PageConfigService);

  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('usa la configuración por defecto cuando no hay nada guardado', () => {
    const service = crear();
    expect(service.isEnabled(ROLES.SUPERADMIN, 'usuarios')).toBeTrue();
    expect(service.isEnabled(ROLES.ADMIN, 'reportes')).toBeTrue();
    expect(service.isEnabled(ROLES.USUARIO, 'sensibles')).toBeFalse();
    expect(service.isEnabled(ROLES.SEGURIDAD, 'guia')).toBeFalse();
  });

  it('devuelve falso para un rol desconocido', () => {
    expect(crear().isEnabled('ROLE_X' as any, 'home')).toBeFalse();
  });

  it('setEnabled actualiza la página y persiste el cambio', () => {
    const service = crear();
    service.setEnabled(ROLES.ADMIN, 'usuarios', true);

    expect(service.isEnabled(ROLES.ADMIN, 'usuarios')).toBeTrue();
    expect(JSON.parse(localStorage.getItem(STORAGE_KEY)!)[ROLES.ADMIN].usuarios).toBeTrue();
  });

  it('setEnabled ignora un rol desconocido', () => {
    const service = crear();
    service.setEnabled('ROLE_X' as any, 'home', true);
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('setAll combina los valores sin perder las demás páginas', () => {
    const service = crear();
    service.setAll(ROLES.USUARIO, { reportes: true, guia: false });

    expect(service.isEnabled(ROLES.USUARIO, 'reportes')).toBeTrue();
    expect(service.isEnabled(ROLES.USUARIO, 'guia')).toBeFalse();
    expect(service.isEnabled(ROLES.USUARIO, 'home')).toBeTrue();
  });

  it('activeCount cuenta las páginas activas de todos los roles', () => {
    const service = crear();
    const inicial = service.activeCount();
    expect(inicial).toBe(12);

    service.setEnabled(ROLES.ADMIN, 'usuarios', true);
    expect(service.activeCount()).toBe(inicial + 1);
  });

  it('combina lo guardado con los valores por defecto', () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ [ROLES.ADMIN]: { home: false } }));
    const service = crear();

    expect(service.isEnabled(ROLES.ADMIN, 'home')).toBeFalse();
    expect(service.isEnabled(ROLES.ADMIN, 'reportes')).toBeTrue();
    expect(service.isEnabled(ROLES.SUPERADMIN, 'home')).toBeTrue();
  });

  it('vuelve a los valores por defecto si lo guardado está corrupto', () => {
    localStorage.setItem(STORAGE_KEY, '{no-es-json');
    expect(crear().isEnabled(ROLES.ADMIN, 'home')).toBeTrue();
  });

  it('no modifica la configuración por defecto compartida entre instancias', () => {
    crear().setEnabled(ROLES.ADMIN, 'home', false);
    localStorage.clear();

    TestBed.resetTestingModule();
    expect(crear().isEnabled(ROLES.ADMIN, 'home')).toBeTrue();
  });
});
