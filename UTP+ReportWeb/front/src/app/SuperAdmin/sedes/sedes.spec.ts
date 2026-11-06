import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Sedes } from './sedes';

describe('Sedes', () => {
  let component: Sedes;
  let fixture: ComponentFixture<Sedes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sedes],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(Sedes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('nombra la lista de sedes (U01)', () => {
    expect(fixture.nativeElement.querySelector('select').getAttribute('aria-label')).toBe('Sede');
  });
});
