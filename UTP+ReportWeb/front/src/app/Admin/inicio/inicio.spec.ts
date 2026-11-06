import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InicioAdmin } from './inicio';

describe('InicioAdmin', () => {
  let component: InicioAdmin;
  let fixture: ComponentFixture<InicioAdmin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InicioAdmin],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

  fixture = TestBed.createComponent(InicioAdmin);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filtra los reportes por texto y por estado (F08)', () => {
    component.reportes.set([
      { id: 1, descripcion: 'Fuga de agua', reporteGestion: { estado: 'RESUELTO' } },
      { id: 2, descripcion: 'Robo de mochila' },
    ] as never);
    fixture.detectChanges();

    const buscador: HTMLInputElement = fixture.nativeElement.querySelector('input[type="search"]');
    buscador.value = 'fuga';
    buscador.dispatchEvent(new Event('input'));
    expect(component.reportesFiltrados().map((r) => r.id)).toEqual([1]);

    component.filtroEstado.set('PENDIENTE');
    expect(component.reportesFiltrados()).toEqual([]);

    component.filtroTexto.set('');
    expect(component.reportesFiltrados().map((r) => r.id)).toEqual([2]);
  });

  it('limita la descripción de la zona a 255 caracteres (F09)', () => {
    component.vista.set('zonas');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.zone-form textarea').maxLength).toBe(255);
    expect(fixture.nativeElement.querySelector('.zone-form').textContent).toContain('0/255');
  });

  describe('palabras muy largas (F12)', () => {
    const LARGA = 'x'.repeat(255);
    const raiz = (): HTMLElement => fixture.nativeElement;

    beforeEach(() => {
      component.cargando.set(false);
      raiz().style.display = 'block';
      raiz().style.width = '390px';
    });

    it('las tarjetas de reportes no ensanchan la página', () => {
      component.reportes.set([{ id: 1, descripcion: LARGA }] as never);
      fixture.detectChanges();
      expect(raiz().querySelector('.report-card')).not.toBeNull();
      expect(raiz().scrollWidth).toBeLessThanOrEqual(raiz().clientWidth + 1);
    });

    it('las tarjetas de zonas no ensanchan la página', () => {
      component.zonas.set([{ id: 1, nombre: LARGA, descripcion: LARGA, sedeId: 1 }] as never);
      component.vista.set('zonas');
      fixture.detectChanges();
      expect(raiz().querySelector('.zone-list article')).not.toBeNull();
      expect(raiz().scrollWidth).toBeLessThanOrEqual(raiz().clientWidth + 1);
    });

    it('el texto largo no apila los botones de la zona', () => {
      component.zonas.set([{ id: 1, nombre: 'Patio', descripcion: LARGA, sedeId: 1 }] as never);
      component.vista.set('zonas');
      fixture.detectChanges();
      raiz().style.width = '1000px';
      // El ancho de la ventana de pruebas no se controla: se fija la disposición de escritorio.
      raiz().querySelector<HTMLElement>('.zone-list article')!.style.flexDirection = 'row';
      const [editar, eliminar] = Array.from(raiz().querySelectorAll<HTMLElement>('.zone-list .actions button'));
      expect(eliminar.getBoundingClientRect().top).toBe(editar.getBoundingClientRect().top);
    });
  });
});
