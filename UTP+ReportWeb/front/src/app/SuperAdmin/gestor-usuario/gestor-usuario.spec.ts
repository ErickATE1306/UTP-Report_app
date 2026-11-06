import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestorUsuario } from './gestor-usuario';

describe('GestorUsuario', () => {
  let component: GestorUsuario;
  let fixture: ComponentFixture<GestorUsuario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestorUsuario],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestorUsuario);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('nombres accesibles (U01)', () => {
    const nombres = () => Array.from<HTMLSelectElement>(fixture.nativeElement.querySelectorAll('select'))
      .map((lista) => lista.getAttribute('aria-label'));

    it('nombra las dos listas de filtro', () => {
      expect(nombres()).toEqual(['Filtrar por rol', 'Filtrar por estado']);
    });

    it('nombra todas las listas del formulario de un usuario', () => {
      component.openCreate();
      component.formData.role = 'usuario';
      fixture.detectChanges();
      expect(nombres()).toEqual(['Filtrar por rol', 'Filtrar por estado', 'Rol', 'Sede', 'Tipo de usuario']);
    });

    it('nombra la lista de zonas y el botón de quitar zona de un agente', () => {
      component.openCreate();
      component.formData.role = 'seguridad';
      component.formData.assignedZones = [{ id: 1, nombre: 'Patio' } as never];
      fixture.detectChanges();
      expect(nombres()).toContain('Agregar zona de la sede');
      expect(fixture.nativeElement.querySelector('.remove-zone-btn').getAttribute('aria-label')).toBe('Quitar zona Patio');
    });

    it('el botón de la contraseña dice lo que hará', () => {
      component.openCreate();
      fixture.detectChanges();
      const boton = (): HTMLButtonElement => fixture.nativeElement.querySelector('.password-toggle-btn');
      expect(boton().getAttribute('aria-label')).toBe('Mostrar contraseña');
      boton().click();
      fixture.detectChanges();
      expect(boton().getAttribute('aria-label')).toBe('Ocultar contraseña');
    });
  });

  describe('eliminar una cuenta (F10)', () => {
    const cuenta = { id: 7, username: 'ana' };
    const peticion = () => TestBed.inject(HttpTestingController)
      .match((r) => r.method === 'DELETE' && r.url.endsWith('/api/usuarios/7'));
    let aviso: jasmine.Spy;

    beforeEach(() => {
      component.users = [cuenta as never];
      aviso = spyOn(window, 'alert');
    });

    it('llama al backend y solo entonces la quita de la lista', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      component.deleteUser(cuenta);
      const [llamada] = peticion();
      expect(component.users.length).toBe(1);

      llamada.flush(null, { status: 204, statusText: 'No Content' });
      expect(component.users).toEqual([]);
      expect(aviso).toHaveBeenCalledWith('Usuario eliminado correctamente.');
    });

    it('conserva la cuenta y muestra el motivo si el backend la rechaza', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      component.deleteUser(cuenta);
      peticion()[0].flush({ message: 'Esta cuenta tiene reportes asociados.' }, { status: 400, statusText: 'Bad Request' });

      expect(component.users.length).toBe(1);
      expect(aviso).toHaveBeenCalledWith('Esta cuenta tiene reportes asociados.');
    });

    it('no hace nada si no se confirma', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      component.deleteUser(cuenta);
      expect(peticion().length).toBe(0);
    });
  });
});
