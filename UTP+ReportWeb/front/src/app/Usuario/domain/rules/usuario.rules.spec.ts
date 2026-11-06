import { EstadoReporte } from '../models/reporte.model';
import { Zona } from '../models/catalogo.model';
import { NotificacionItem } from '../models/notificacion.model';
import { unDetallado, unNuevoReporte, unaFoto } from '../../testing/usuario.fixtures';
import {
  agruparPorDia,
  claveNotificacion,
  filtrarNotificaciones,
  reporteANotificacion,
} from './notificacion.rules';
import {
  DESCRIPCION_MAX,
  FOTO_MAX_BYTES,
  codigoReporte,
  construirSeguimiento,
  contarReportes,
  esActivo,
  etiquetaEstado,
  etiquetaPrioridad,
  filtrarReportes,
  progresoEstado,
  validarFoto,
  validarNuevoReporte,
} from './reporte.rules';
import { etiquetaZona, filtrarZonas, resumirZonas } from './zona.rules';

const ORDEN: EstadoReporte[] = ['PENDIENTE', 'EN_PROCESO', 'UBICANDO', 'INVESTIGANDO', 'PENDIENTE_APROBACION', 'RESUELTO'];

describe('reporte.rules', () => {
  it('el progreso crece en el orden del flujo y llega a 100 al resolverse', () => {
    const progresos = ORDEN.map(progresoEstado);
    expect(progresos).toEqual([...progresos].sort((a, b) => a - b));
    expect(new Set(progresos).size).toBe(ORDEN.length);
    expect(progresoEstado('RESUELTO')).toBe(100);
  });

  it('un reporte cancelado no muestra progreso', () => {
    expect(progresoEstado('CANCELADO')).toBe(0);
  });

  it('etiqueta estados y prioridades, incluso sin prioridad asignada', () => {
    expect(etiquetaEstado('INVESTIGANDO')).toBe('En investigación');
    expect(etiquetaPrioridad('ALTA')).toBe('Alta');
    expect(etiquetaPrioridad(null)).toBe('Por asignar');
  });

  it('forma el código visible del reporte', () => {
    expect(codigoReporte(1024)).toBe('#R-1024');
  });

  it('solo RESUELTO y CANCELADO dejan de estar activos', () => {
    expect(ORDEN.slice(0, 5).every(esActivo)).toBeTrue();
    expect(esActivo('RESUELTO')).toBeFalse();
    expect(esActivo('CANCELADO')).toBeFalse();
  });

  it('un cancelado cuenta en el total, pero no como activo ni como resuelto', () => {
    const conteo = contarReportes([{ estado: 'INVESTIGANDO' }, { estado: 'RESUELTO' }, { estado: 'CANCELADO' }]);
    expect(conteo).toEqual({ total: 3, activos: 1, resueltos: 1 });
  });

  describe('filtrarReportes', () => {
    const activo = unDetallado({ id: 1, estado: 'EN_PROCESO', tipoNombre: 'Robo', zonaNombre: 'Patio' });
    const resuelto = unDetallado({ id: 2, estado: 'RESUELTO', tipoNombre: 'Accidente', zonaNombre: 'Aulas' });
    const cancelado = unDetallado({ id: 3, estado: 'CANCELADO', tipoNombre: 'Otro', zonaNombre: 'Patio' });
    const todos = [activo, resuelto, cancelado];

    it('filtra por categoría', () => {
      expect(filtrarReportes(todos, 'TODOS', '')).toEqual(todos);
      expect(filtrarReportes(todos, 'ACTIVOS', '')).toEqual([activo]);
      expect(filtrarReportes(todos, 'RESUELTOS', '')).toEqual([resuelto]);
    });

    it('busca sin distinguir mayúsculas por tipo, zona o código', () => {
      expect(filtrarReportes(todos, 'TODOS', '  ACCID ')).toEqual([resuelto]);
      expect(filtrarReportes(todos, 'TODOS', 'patio')).toEqual([activo, cancelado]);
      expect(filtrarReportes(todos, 'TODOS', '#r-3')).toEqual([cancelado]);
    });

    it('combina categoría y búsqueda', () => {
      expect(filtrarReportes(todos, 'ACTIVOS', 'aulas')).toEqual([]);
    });
  });

  describe('construirSeguimiento', () => {
    it('marca los pasos anteriores, el actual y los pendientes', () => {
      const pasos = construirSeguimiento('INVESTIGANDO').map((paso) => paso.estado);
      expect(pasos).toEqual(['completado', 'completado', 'completado', 'actual', 'pendiente', 'pendiente']);
    });

    it('al inicio solo está activo el primer paso', () => {
      expect(construirSeguimiento('PENDIENTE')[0].estado).toBe('actual');
    });

    it('un reporte resuelto tiene todos los pasos completados', () => {
      expect(construirSeguimiento('RESUELTO').every((paso) => paso.estado === 'completado')).toBeTrue();
    });

    it('un reporte cancelado termina en un paso de cancelación', () => {
      const pasos = construirSeguimiento('CANCELADO');
      expect(pasos.length).toBe(2);
      expect(pasos[1].titulo).toBe('Reporte cancelado');
      expect(pasos[1].estado).toBe('actual');
    });
  });

  describe('validarFoto', () => {
    it('acepta la ausencia de foto, JPG y PNG', () => {
      expect(validarFoto(null)).toBeNull();
      expect(validarFoto(unaFoto('image/jpeg'))).toBeNull();
      expect(validarFoto(unaFoto('image/png'))).toBeNull();
    });

    it('rechaza otros formatos, igual que ReporteFileValidator del backend', () => {
      expect(validarFoto(unaFoto('application/pdf'))).toBe('La foto debe ser JPG o PNG.');
    });

    it('acepta exactamente 10 MB y rechaza un byte más', () => {
      expect(validarFoto(unaFoto('image/png', FOTO_MAX_BYTES))).toBeNull();
      expect(validarFoto(unaFoto('image/png', FOTO_MAX_BYTES + 1))).toBe('La foto no puede superar los 10 MB.');
    });
  });

  describe('validarNuevoReporte', () => {
    it('no reporta errores si los datos son válidos', () => {
      expect(validarNuevoReporte(unNuevoReporte())).toEqual({});
    });

    it('exige tipo, zona y descripción', () => {
      const errores = validarNuevoReporte(unNuevoReporte({ tipoIncidenteId: null, zonaId: null, descripcion: '   ' }));
      expect(Object.keys(errores).sort()).toEqual(['descripcion', 'tipoIncidenteId', 'zonaId']);
    });

    it('limita la descripción sin contar los espacios de los extremos', () => {
      expect(validarNuevoReporte(unNuevoReporte({ descripcion: ` ${'x'.repeat(DESCRIPCION_MAX)} ` }))).toEqual({});
      expect(validarNuevoReporte(unNuevoReporte({ descripcion: 'x'.repeat(DESCRIPCION_MAX + 1) })).descripcion).toContain('máximo');
    });

    it('no admite más caracteres que la columna descripcion del backend', () => {
      expect(DESCRIPCION_MAX).toBe(255);
    });

    it('incluye el error de la foto', () => {
      expect(validarNuevoReporte(unNuevoReporte({ foto: unaFoto('text/plain') })).foto).toBeDefined();
    });
  });
});

describe('zona.rules', () => {
  const zona = (estado: Zona['estado'], nombre = 'Zona'): Zona => ({ id: 1, nombre, descripcion: '', sedeId: 1, estado });

  it('etiqueta los estados y no presenta como segura una zona sin estado', () => {
    expect(etiquetaZona('ZONA_PELIGROSA')).toBe('Zona peligrosa');
    expect(etiquetaZona(null)).toBe('Sin información');
  });

  it('el campus está operativo solo si todas las zonas son seguras', () => {
    const resumen = resumirZonas([zona('ZONA_SEGURA'), zona('ZONA_SEGURA')]);
    expect(resumen.nivel).toBe('operativo');
    expect(resumen.seguras).toBe(2);
  });

  it('una zona en precaución cambia el nivel del campus', () => {
    expect(resumirZonas([zona('ZONA_SEGURA'), zona('ZONA_PRECAUCION')]).nivel).toBe('precaucion');
  });

  it('el peligro tiene prioridad sobre la precaución', () => {
    const resumen = resumirZonas([zona('ZONA_PRECAUCION'), zona('ZONA_PELIGROSA'), zona('ZONA_SEGURA')]);
    expect(resumen.nivel).toBe('peligro');
    expect([resumen.seguras, resumen.precaucion, resumen.peligrosas]).toEqual([1, 1, 1]);
  });

  it('no declara el campus operativo si faltan datos', () => {
    expect(resumirZonas([zona('ZONA_SEGURA'), zona(null)]).nivel).toBe('sin-datos');
    expect(resumirZonas([]).nivel).toBe('sin-datos');
  });

  it('busca zonas por nombre', () => {
    const patio = zona('ZONA_SEGURA', 'Patio principal');
    expect(filtrarZonas([patio, zona('ZONA_SEGURA', 'Aulas')], ' PATIO ')).toEqual([patio]);
  });
});

describe('notificacion.rules', () => {
  const item = (cambios: Partial<NotificacionItem>): NotificacionItem =>
    ({ id: 1, title: 't', body: 'b', ts: new Date(2026, 0, 10, 12), read: false, ...cambios });

  it('crea la notificación con los datos reales del reporte', () => {
    const reporte = unDetallado();
    const notificacion = reporteANotificacion(reporte, new Set());
    expect(notificacion.title).toContain('en investigación');
    expect(notificacion.body).toContain('#R-1024');
    expect(notificacion.ts).toBe(reporte.fechaActualizacion);
    expect(notificacion.read).toBeFalse();
  });

  it('vuelve a estar sin leer cuando el reporte cambia de estado', () => {
    const leidas = new Set([claveNotificacion({ id: 1024, estado: 'INVESTIGANDO' })]);
    expect(reporteANotificacion(unDetallado(), leidas).read).toBeTrue();
    expect(reporteANotificacion(unDetallado({ estado: 'RESUELTO' }), leidas).read).toBeFalse();
  });

  it('separa reportes y zonas', () => {
    const reporte = item({ id: 1 });
    const zona = item({ id: 2, zonaNombre: 'Patio' });
    expect(filtrarNotificaciones([reporte, zona], 'TODAS')).toEqual([reporte, zona]);
    expect(filtrarNotificaciones([reporte, zona], 'REPORTES')).toEqual([reporte]);
    expect(filtrarNotificaciones([reporte, zona], 'ZONAS')).toEqual([zona]);
  });

  it('agrupa por día, omite grupos vacíos y ordena del más reciente al más antiguo', () => {
    const hoy = new Date(2026, 0, 10, 23, 59);
    const temprano = item({ id: 1, ts: new Date(2026, 0, 10, 0, 1) });
    const tarde = item({ id: 2, ts: new Date(2026, 0, 10, 20) });
    const ayer = item({ id: 3, ts: new Date(2026, 0, 9, 23, 59) });
    const antigua = item({ id: 4, ts: new Date(2025, 11, 1) });

    const grupos = agruparPorDia([antigua, temprano, ayer, tarde], hoy);

    expect(grupos.map((grupo) => grupo.titulo)).toEqual(['Hoy', 'Ayer', 'Anteriores']);
    expect(grupos[0].items).toEqual([tarde, temprano]);
    expect(agruparPorDia([ayer], hoy).map((grupo) => grupo.titulo)).toEqual(['Ayer']);
  });
});
