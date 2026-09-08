import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminLayoutComponent } from '../../shared/layout/admin-layout.component';
import { InicioAdmin } from '../../Admin/inicio/inicio';

const routes: Routes = [{ path: '', component: AdminLayoutComponent, children: [{ path: '', component: InicioAdmin }] }];
@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class AdminRoutingModule {}
