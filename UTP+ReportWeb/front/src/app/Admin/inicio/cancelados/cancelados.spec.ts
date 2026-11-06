import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Cancelados } from './cancelados';

describe('Cancelados', () => {
  let component: Cancelados;
  let fixture: ComponentFixture<Cancelados>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cancelados],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(Cancelados);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
