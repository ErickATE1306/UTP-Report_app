import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { delay } from 'rxjs/operators';

// Base de datos estática simulada en memoria para la sesión
let mockZonas = [
  { id: 1, nombre: 'Pabellón A - Torre Central', descripcion: 'Zona de aulas principales y laboratorios de sistemas', sedeId: 1, activo: true, estado: 'ZONA_SEGURA', reportCount: 1 },
  { id: 2, nombre: 'Pabellón B - Biblioteca', descripcion: 'Área de estudio silencioso, salas de lectura y cómputo', sedeId: 1, activo: true, estado: 'ZONA_PRECAUCION', reportCount: 3 },
  { id: 3, nombre: 'Estacionamiento Norte', descripcion: 'Zona posterior de parqueo de vehículos y motos', sedeId: 1, activo: true, estado: 'ZONA_PELIGROSA', reportCount: 5 },
  { id: 4, nombre: 'Cafetería Central', descripcion: 'Comedor principal del campus central', sedeId: 1, activo: true, estado: 'ZONA_SEGURA', reportCount: 0 },
  { id: 5, nombre: 'Laboratorios de Ingeniería', descripcion: 'Laboratorios especializados de electrónica y robótica', sedeId: 1, activo: true, estado: 'ZONA_SEGURA', reportCount: 2 }
];

let mockUsuarios = [
  { id: 1, username: 'admin.utp', nombreCompleto: 'Carlos Administrador', correo: 'carlos.admin@utp.edu.pe', estado: 'ACTIVO', roles: ['ROLE_ADMIN'], tipoUsuario: 'ADMINISTRATIVO' },
  { id: 2, username: 'maria.alumno', nombreCompleto: 'María López', correo: 'a12345678@utp.edu.pe', estado: 'ACTIVO', roles: ['ROLE_USUARIO'], tipoUsuario: 'ALUMNO' },
  { id: 3, username: 'juan.seguridad', nombreCompleto: 'Juan Pérez (Oficial)', correo: 'juan.seg@utp.edu.pe', estado: 'ACTIVO', roles: ['ROLE_SEGURIDAD'], tipoUsuario: 'SEGURIDAD' },
  { id: 4, username: 'super.admin', nombreCompleto: 'Super Admin Sistema', correo: 'superadmin@utp.edu.pe', estado: 'ACTIVO', roles: ['ROLE_SUPERADMIN'], tipoUsuario: 'SUPERADMIN' }
];

let mockReportes: any[] = [
  {
    id: 101,
    tipoIncidenteId: 1,
    zonaId: 3,
    descripcion: 'Se reportó intento de hurto de casco de motocicleta cerca al portón posterior.',
    fechaCreacion: '2026-09-07T18:30:00',
    isAnonimo: false,
    contacto: '987654321',
    usuarioId: 2,
    ultimoEstado: 'PENDIENTE',
    ultimaPrioridad: 'ALTA',
    mensajeSeguridad: 'Patrullaje asignado a la zona.',
    reporteGestion: { id: 201, reporteId: 101, estado: 'PENDIENTE', prioridad: 'ALTA', fechaActualizacion: '2026-09-07T18:35:00' }
  },
  {
    id: 102,
    tipoIncidenteId: 3,
    zonaId: 2,
    descripcion: 'Luz parpadeante y cable expuesto en el pasadizo del segundo piso de la biblioteca.',
    fechaCreacion: '2026-09-07T15:10:00',
    isAnonimo: true,
    usuarioId: 2,
    ultimoEstado: 'EN_GESTION',
    ultimaPrioridad: 'MEDIA',
    mensajeSeguridad: 'Personal de mantenimiento en ruta.',
    reporteGestion: { id: 202, reporteId: 102, estado: 'EN_GESTION', prioridad: 'MEDIA', fechaActualizacion: '2026-09-07T16:00:00' }
  },
  {
    id: 103,
    tipoIncidenteId: 4,
    zonaId: 1,
    descripcion: 'Estudiante presentó mareo en el aula A-402, requiere auxilio médico.',
    fechaCreacion: '2026-09-07T12:00:00',
    isAnonimo: false,
    contacto: '912345678',
    usuarioId: 2,
    ultimoEstado: 'COMPLETADO',
    ultimaPrioridad: 'ALTA',
    mensajeSeguridad: 'Atendido por enfermería del campus. Estudiante estable.',
    reporteGestion: { id: 203, reporteId: 103, estado: 'COMPLETADO', prioridad: 'ALTA', fechaActualizacion: '2026-09-07T12:45:00' }
  }
];

const mockTiposIncidentes = [
  { id: 1, nombre: 'Robo / Hurto', descripcion: 'Sustracción de pertenencias o accesorios' },
  { id: 2, nombre: 'Acoso o Agresión', descripcion: 'Conductas inapropiadas o faltas de respeto' },
  { id: 3, nombre: 'Infraestructura o Riesgo Eléctrico', descripcion: 'Daños físicas en ambientes o instalaciones' },
  { id: 4, nombre: 'Emergencia Médica / Primeros Auxilios', descripcion: 'Malestar de salud o accidentes' },
  { id: 5, nombre: 'Actividad Sospechosa', descripcion: 'Personas ajenas o comportamientos inusuales' }
];

const mockSedes = [
  { id: 1, nombre: 'UTP Lima Centro', direccion: 'Av. Arequipa 265, Lima' },
  { id: 2, nombre: 'UTP Lima Norte', direccion: 'Av. Alfredo Mendiola 6377, Los Olivos' },
  { id: 3, nombre: 'UTP Ate', direccion: 'Carretera Central Km 10.3, Ate' },
  { id: 4, nombre: 'UTP San Juan de Lurigancho', direccion: 'Av. el Sol 329, SJL' }
];

export const mockBackendInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const url = req.url;

  // Interceptar sólo peticiones a la API local (localhost:8080 o /api/)
  if (!url.includes(':8080') && !url.includes('/api/')) {
    return next(req);
  }

  let body: any = null;
  let status = 200;

  // 1. Auth Login
  if (url.includes('/api/auth/login')) {
    body = {
      token: 'mock-jwt-token-xyz-12345',
      tipoToken: 'Bearer',
      roles: ['ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_USUARIO', 'ROLE_SEGURIDAD']
    };
  }
  // 2. Perfil Usuario me
  else if (url.includes('/api/usuarios/me')) {
    body = {
      id: 1,
      username: 'alexis.demo',
      nombreCompleto: 'Alexis Usuario Demo',
      correo: 'alexis.demo@utp.edu.pe',
      tipoUsuario: 'ALUMNO',
      roles: ['ROLE_SUPERADMIN', 'ROLE_ADMIN', 'ROLE_USUARIO', 'ROLE_SEGURIDAD']
    };
  }
  // 3. Usuarios
  else if (url.includes('/api/usuarios')) {
    body = mockUsuarios;
  }
  // 4. Zonas
  else if (url.includes('/api/zonas')) {
    if (req.method === 'POST') {
      const newZone = {
        id: Date.now(),
        nombre: 'Nueva Zona Demo',
        descripcion: 'Zona agregada en prototipo estático',
        sedeId: 1,
        activo: true,
        estado: 'ZONA_SEGURA',
        reportCount: 0
      };
      mockZonas.push(newZone);
      body = newZone;
    } else if (req.method === 'DELETE') {
      const parts = url.split('/');
      const id = parseInt(parts[parts.length - 1], 10);
      mockZonas = mockZonas.filter(z => z.id !== id);
      body = { message: 'Zona eliminada correctamente' };
      return of(new HttpResponse({ status: 204, body: null })).pipe(delay(50));
    } else {
      body = mockZonas;
    }
  }
  // 5. Reportes
  else if (url.includes('/api/reportes')) {
    if (req.method === 'POST' && !url.includes('/gestion/')) {
      const newRep = {
        id: Date.now(),
        tipoIncidenteId: 1,
        zonaId: 1,
        descripcion: 'Nuevo reporte estático generado desde el prototipo.',
        fechaCreacion: new Date().toISOString(),
        isAnonimo: false,
        usuarioId: 1,
        ultimoEstado: 'PENDIENTE',
        ultimaPrioridad: 'ALTA',
        reporteGestion: { id: Date.now() + 1, reporteId: Date.now(), estado: 'PENDIENTE', prioridad: 'ALTA', fechaActualizacion: new Date().toISOString() }
      };
      mockReportes.unshift(newRep);
      body = newRep;
    } else if (url.includes('/gestion/')) {
      body = {
        id: Date.now(),
        reporteId: 101,
        estado: 'COMPLETADO',
        prioridad: 'ALTA',
        fechaActualizacion: new Date().toISOString()
      };
    } else {
      body = mockReportes;
    }
  }
  // 6. Tipos de Incidentes
  else if (url.includes('/api/tipoincidentes')) {
    body = mockTiposIncidentes;
  }
  // 7. Sedes
  else if (url.includes('/api/sedes')) {
    body = mockSedes;
  }
  // Fallback genérico para cualquier otro endpoint
  else {
    body = { success: true, message: 'Respuesta simulada estática' };
  }

  return of(new HttpResponse({ status, body })).pipe(delay(50));
};
