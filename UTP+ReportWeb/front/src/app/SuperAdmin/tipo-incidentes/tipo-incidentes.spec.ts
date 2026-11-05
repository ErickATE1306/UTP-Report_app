import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TipoIncidentes } from './tipo-incidentes';

describe('TipoIncidentes', () => {
  let component: TipoIncidentes;
  let fixture: ComponentFixture<TipoIncidentes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TipoIncidentes],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(TipoIncidentes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
