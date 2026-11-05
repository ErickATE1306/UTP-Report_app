import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AsignarSeguridad } from './asignar-seguridad';

describe('AsignarSeguridad', () => {
  let component: AsignarSeguridad;
  let fixture: ComponentFixture<AsignarSeguridad>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AsignarSeguridad],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AsignarSeguridad);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
