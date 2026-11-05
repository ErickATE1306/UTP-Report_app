import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-guia-seguridad',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="u-pagina">
      <div>
        <h1>Guía de seguridad</h1>
        <p class="u-tenue">Aprende a usar UTP Reporta</p>
      </div>

      <div class="temas">
        @for (tema of temas; track tema.titulo; let i = $index) {
          <details class="u-card tema">
            <summary>
              <span class="numero" aria-hidden="true">{{ i + 1 }}</span>
              <span class="titulo">{{ tema.titulo }}</span>
              <i class="bi bi-chevron-down" aria-hidden="true"></i>
            </summary>
            <p>{{ tema.contenido }}</p>
          </details>
        }
      </div>

      <section class="emergencia">
        <h2>En caso de emergencia</h2>
        <p class="u-tenue">Si hay peligro inmediato, aléjate del lugar y avisa al personal de seguridad más cercano. Después, registra lo ocurrido.</p>
        <a class="u-btn" routerLink="/usuario/nuevo-reporte">
          <i class="bi bi-exclamation-triangle" aria-hidden="true"></i> Reportar ahora
        </a>
      </section>
    </div>
  `,
  styleUrls: ['../shared/usuario-ui.scss'],
  styles: [`
    .u-pagina { max-width: 720px; margin: 0 auto; width: 100%; }
    .temas { display: grid; gap: 10px; }
    .tema { padding: 0; }
    summary { display: flex; align-items: center; gap: 12px; min-height: 52px; padding: 10px 14px; font-weight: 600; cursor: pointer; list-style: none; }
    summary::-webkit-details-marker { display: none; }
    .numero { display: grid; place-items: center; flex: none; width: 30px; height: 30px; border-radius: 50%; background: var(--u-indigo-suave); color: var(--u-indigo); }
    .titulo { flex: 1; }
    summary i { color: var(--u-indigo); transition: transform .15s; }
    details[open] summary i { transform: rotate(180deg); }
    details p { padding: 0 14px 14px 56px; color: var(--u-tenue); font-size: .9rem; }
    .emergencia { display: grid; gap: 10px; }
    @media (prefers-reduced-motion: reduce) { summary i { transition: none; } }
  `],
})
export class GuiaSeguridadComponent {
  readonly temas = [
    {
      titulo: 'Cómo crear un reporte',
      contenido: 'Pulsa "Reportar", elige el tipo de incidente, describe qué ocurrió, indica la sede y la zona, y confirma el envío. Puedes adjuntar una foto JPG o PNG de hasta 10 MB. Cada cuenta puede enviar hasta 3 reportes por día.',
    },
    {
      titulo: 'Cómo se asigna la prioridad',
      contenido: 'La prioridad (baja, media o alta) la define el equipo que gestiona tu reporte según su gravedad. Podrás verla en el detalle en cuanto se asigne.',
    },
    {
      titulo: 'Seguimiento del caso',
      contenido: 'En "Mis reportes" puedes ver el avance de cada caso: reporte recibido, seguridad asignada, personal en camino, investigación, aprobación y resolución.',
    },
    {
      titulo: 'Estado de zonas',
      contenido: 'Consulta por sede qué zonas están seguras, en precaución o peligrosas antes de desplazarte por el campus.',
    },
    {
      titulo: 'Notificaciones',
      contenido: 'La campana del encabezado se marca cuando alguno de tus reportes tiene un estado que aún no has revisado.',
    },
  ];
}
