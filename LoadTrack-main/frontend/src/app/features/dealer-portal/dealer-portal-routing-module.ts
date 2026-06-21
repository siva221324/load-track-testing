import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DealerDashboard } from './dealer-dashboard';

const routes: Routes = [
  { path: '', component: DealerDashboard }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DealerPortalRoutingModule { }
