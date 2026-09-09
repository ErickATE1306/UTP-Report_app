import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

export interface PerfilUsuario {
  username: string;
  nombreCompleto: string;
  tipoUsuario?: string; // ALUMNO | DOCENTE | null
  roles: string[];
}

@Injectable({providedIn: 'root'})
export class PerfilService {
  private baseUrl = 'http://localhost:8080/api/usuarios/me';
  perfil = signal<PerfilUsuario | null>(null);

  constructor(private http: HttpClient, private auth: AuthService) {}

  cargarPerfil(){
    const mockDefault: PerfilUsuario = {
      username: 'alexis.demo',
      nombreCompleto: 'Alexis Usuario Demo',
      tipoUsuario: 'ALUMNO',
      roles: ['ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_USUARIO', 'ROLE_SEGURIDAD']
    };

    if(!this.auth.getToken()) {
      this.perfil.set(mockDefault);
      return;
    }
    this.http.get<PerfilUsuario>(this.baseUrl).subscribe({
      next: p => this.perfil.set(p),
      error: _ => this.perfil.set(mockDefault)
    });
  }

  obtenerPerfil() {
    return this.http.get<PerfilUsuario>(this.baseUrl);
  }
}
