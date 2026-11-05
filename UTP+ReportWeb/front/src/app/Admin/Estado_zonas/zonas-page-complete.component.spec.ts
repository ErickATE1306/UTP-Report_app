import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideUsuarioModule } from '../../Usuario/infrastructure/usuario.providers';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminZonasPageCompleteComponent } from './zonas-page-complete.component';

describe('AdminZonasPageCompleteComponent', () => {
  let component: AdminZonasPageCompleteComponent;
  let fixture: ComponentFixture<AdminZonasPageCompleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminZonasPageCompleteComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideUsuarioModule()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminZonasPageCompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
