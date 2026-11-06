import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';
import { superadminGuard } from './core/auth/superadmin.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { Inicio as SuperAdminInicio } from './SuperAdmin/inicio/inicio';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'admin', canActivate: [adminGuard], loadChildren: () => import('./features/admin/admin.module').then((module) => module.AdminModule) },
	{ path: 'usuario', loadChildren: () => import('./Usuario/usuario.routes').then((module) => module.USUARIO_ROUTES) },
	{ path: 'superadmin/dashboard/:tab', canActivate: [superadminGuard], component: SuperAdminInicio },
	{ path: 'superadmin/dashboard', canActivate: [superadminGuard], component: SuperAdminInicio },
	{ path: '', pathMatch: 'full', redirectTo: 'admin' },
	{ path: '**', redirectTo: 'admin' }

];
