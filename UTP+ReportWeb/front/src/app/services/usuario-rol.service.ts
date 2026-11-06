import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Sede } from './sede.service';
import { Zona } from './zona.service';

/*export interface Sede {
  id: number;
  nombre: string;
}*/
export interface RegistroDTO {
  nombreCompleto: string;
  username: string;
  correo: string;
  password: string;
  telefono: string;
  tipoUsuario: string; // puede ser "USER", "ADMIN", "SECURITY", etc.
 sedeId: number; 

}
export interface RegistroAdminDTO {
  nombreCompleto: string;
  username: string;
  correo: string;
  password: string;
  telefono: string;
  sedeId: number; 
}
export interface RegistroSecurityDTO {
    nombreCompleto: string;
    username: string;
    correo: string;
    password: string;
    telefono: string;
    // 🔑 CAMBIO: Enviar solo el ID de la sede (sedeId: Long)
    sedeId: number; 
    // 🔑 CAMBIO: Enviar un array/set de IDs de zona (zonaIds: Set<Long>)
    zonaIds: number[]; 
}


// Estas peticiones se piden como texto, así que un error llega con su JSON sin interpretar.
// Se convierte para que las pantallas puedan leer err.error.message.
function conErrorLegible(err: HttpErrorResponse): HttpErrorResponse {
  if (typeof err.error !== 'string') return err;
  try {
    return new HttpErrorResponse({
      error: JSON.parse(err.error),
      headers: err.headers,
      status: err.status,
      statusText: err.statusText,
      url: err.url ?? undefined,
    });
  } catch {
    return err;
  }
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioRolService {
  private baseUrl = 'http://localhost:8080/api/auth';
  constructor(private http: HttpClient) { }
  registrarUsuario(data: RegistroDTO): Observable<string> {
    return this.registrar('registrarUsuario', data);
  }
  registrarAdmin(data: RegistroAdminDTO): Observable<string> {
    return this.registrar('registrarAdmin', data);
  }
  registrarSecurity(data: RegistroSecurityDTO): Observable<string> {
    return this.registrar('registrarSeguridad', data);
  }
  private registrar(ruta: string, data: unknown): Observable<string> {
    return this.http.post(`${this.baseUrl}/${ruta}`, data, { responseType: 'text' }).pipe(
      catchError((err: HttpErrorResponse) => throwError(() => conErrorLegible(err)))
    );
  }


}
