import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportesSensibles } from './reportes-sensibles';

describe('ReportesSensibles', () => {
  let component: ReportesSensibles;
  let fixture: ComponentFixture<ReportesSensibles>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportesSensibles],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportesSensibles);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('nombra las tres listas de filtro (U01)', () => {
    component.accesoConcedido = true;
    component.showAuthModal = false;
    fixture.detectChanges();
    const nombres = Array.from<HTMLSelectElement>(fixture.nativeElement.querySelectorAll('.filters select'))
      .map((lista) => lista.getAttribute('aria-label'));
    expect(nombres).toEqual(['Filtrar por estado', 'Filtrar por prioridad', 'Filtrar por tipo de incidente']);
  });
});
