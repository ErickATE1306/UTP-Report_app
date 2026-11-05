import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { DarkModeService } from './dark-mode.service';
import { PerfilService } from './perfil.service';

describe('DarkModeService', () => {
  const STORAGE_KEY = 'utp_dark_mode_map';
  let rolesActivos: string[];

  const crear = () => {
    const service = TestBed.inject(DarkModeService);
    TestBed.tick();
    return service;
  };
  const tieneClaseOscura = () => document.documentElement.classList.contains('dark-mode');

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-mode');
    document.body.classList.remove('dark-mode');
    rolesActivos = [];

    // Mismo contrato que AuthService real: los roles llevan el prefijo ROLE_
    const auth = {
      roles: signal<string[]>([]),
      hasRole: (rol: string) => rolesActivos.includes(rol),
      isAdminAsUser: () => false,
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: PerfilService, useValue: { perfil: signal(null) } },
        { provide: Router, useValue: { url: '/admin', events: new Subject() } },
      ],
    });
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-mode');
    document.body.classList.remove('dark-mode');
  });

  it('inicia en modo claro por defecto', () => {
    expect(crear().darkMode()).toBeFalse();
    expect(tieneClaseOscura()).toBeFalse();
  });

  it('toggleDarkMode activa el modo oscuro y agrega la clase global', () => {
    const service = crear();

    service.toggleDarkMode();
    TestBed.tick();

    expect(service.darkMode()).toBeTrue();
    expect(tieneClaseOscura()).toBeTrue();
  });

  it('persiste la preferencia en localStorage', () => {
    const service = crear();

    service.toggleDarkMode();
    TestBed.tick();

    expect(Object.values(JSON.parse(localStorage.getItem(STORAGE_KEY)!))).toContain(true);
  });

  it('tolera un mapa de preferencias corrupto', () => {
    localStorage.setItem(STORAGE_KEY, '{no-es-json');
    expect(crear().darkMode()).toBeFalse();
  });

  it('F04: un SUPERADMIN (ROLE_SUPERADMIN) no puede activar el modo oscuro', () => {
    rolesActivos = ['ROLE_SUPERADMIN'];
    const service = crear();

    service.toggleDarkMode();
    TestBed.tick();

    expect(service.darkMode()).toBeFalse();
    expect(tieneClaseOscura()).toBeFalse();
  });

  for (const [rol, clave] of [['ROLE_ADMIN', 'ADMIN'], ['ROLE_SEGURIDAD', 'SEGURIDAD'], ['ROLE_USUARIO', 'USUARIO']]) {
    it(`F04: guarda la preferencia por rol real (${rol})`, () => {
      rolesActivos = [rol];
      const service = crear();

      service.toggleDarkMode();
      TestBed.tick();

      expect(JSON.parse(localStorage.getItem(STORAGE_KEY)!)[clave]).toBeTrue();
    });
  }
});
