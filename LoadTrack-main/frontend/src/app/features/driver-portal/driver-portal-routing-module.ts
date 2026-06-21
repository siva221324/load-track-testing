import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DriverDashboard } from './driver-dashboard';

const routes: Routes = [
  { path: '', component: DriverDashboard }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DriverPortalRoutingModule { }
