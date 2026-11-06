/** Encabezados del CONNECT de STOMP, o null si no hay sesión: el servidor rechaza conexiones sin token. */
export function encabezadosStomp(): Record<string, string> | null {
  const token = localStorage.getItem('auth_token');
  return token ? { Authorization: `Bearer ${token}` } : null;
}
