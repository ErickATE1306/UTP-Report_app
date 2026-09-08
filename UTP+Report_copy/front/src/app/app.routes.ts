import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin.guard';
import { LoginComponent } from './features/auth/login/login.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'admin', canActivate: [adminGuard], loadChildren: () => import('./features/admin/admin.module').then((module) => module.AdminModule) },
	{ path: '', pathMatch: 'full', redirectTo: 'admin' },
	{ path: '**', redirectTo: 'admin' }
];
