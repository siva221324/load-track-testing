import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminRequests } from './admin-requests';

const routes: Routes = [
  { path: '', component: AdminRequests }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TripRequestsRoutingModule { }
