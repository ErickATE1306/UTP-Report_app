import { Routes } from '@angular/router';
import { InicioSesion } from './Login/inicio-sesion/inicio-sesion';
import { Inicio as SuperAdminInicio } from './SuperAdmin/inicio/inicio';
import { roleGuard } from './guards/role.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
	{ path: 'login', component: InicioSesion, canActivate: [guestGuard] },
	{ path: 'superadmin/dashboard/:tab', component: SuperAdminInicio, canActivate: [roleGuard], data: { roles: ['ROLE_SUPERADMIN'] } },
	{ path: 'superadmin/dashboard', component: SuperAdminInicio, canActivate: [roleGuard], data: { roles: ['ROLE_SUPERADMIN'] } },
	{ path: '', pathMatch: 'full', redirectTo: 'superadmin/dashboard' },
	{ path: '**', redirectTo: 'superadmin/dashboard' }
];
