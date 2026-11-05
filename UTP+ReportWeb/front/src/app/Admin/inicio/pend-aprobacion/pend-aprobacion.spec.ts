import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PendAprobacion } from './pend-aprobacion';

describe('PendAprobacion', () => {
  let component: PendAprobacion;
  let fixture: ComponentFixture<PendAprobacion>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PendAprobacion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(PendAprobacion);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
