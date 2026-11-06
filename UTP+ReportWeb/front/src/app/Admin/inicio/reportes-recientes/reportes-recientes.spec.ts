import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportesRecientes } from './reportes-recientes';

describe('ReportesRecientes', () => {
  let component: ReportesRecientes;
  let fixture: ComponentFixture<ReportesRecientes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportesRecientes],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportesRecientes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
