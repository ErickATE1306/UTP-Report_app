import { ErroresReporte } from '../domain/rules/reporte.rules';

export class ReporteInvalidoError extends Error {
  constructor(readonly errores: ErroresReporte) {
    super('Revisa los datos del reporte.');
    this.name = 'ReporteInvalidoError';
  }
}

export class ReporteNoEncontradoError extends Error {
  constructor() {
    super('No encontramos este reporte entre los tuyos.');
    this.name = 'ReporteNoEncontradoError';
  }
}
