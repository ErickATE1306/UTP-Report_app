import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthService } from '../../services/auth.service';
import { Inicio } from './inicio';

describe('Inicio', () => {
  let component: Inicio;
  let fixture: ComponentFixture<Inicio>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inicio],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(Inicio);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('el botón Cerrar sesión termina la sesión (F11)', () => {
    const salir = spyOn(TestBed.inject(AuthService), 'logout');
    const boton: HTMLButtonElement = fixture.nativeElement.querySelector('.logout-button');
    expect(boton.textContent).toContain('Cerrar sesión');

    boton.click();

    expect(salir).toHaveBeenCalledTimes(1);
  });
});
