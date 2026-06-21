import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DealerRequests } from './dealer-requests';

const routes: Routes = [
  { path: '', component: DealerRequests }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DealerRequestsRoutingModule { }
