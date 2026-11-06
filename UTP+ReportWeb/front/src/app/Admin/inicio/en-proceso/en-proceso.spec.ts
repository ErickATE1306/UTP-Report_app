import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnProceso } from './en-proceso';

describe('EnProceso', () => {
  let component: EnProceso;
  let fixture: ComponentFixture<EnProceso>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnProceso],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnProceso);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
