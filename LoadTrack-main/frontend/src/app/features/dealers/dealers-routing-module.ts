import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DealerList } from './dealer-list';

const routes: Routes = [
  { path: '', component: DealerList }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DealersRoutingModule { }
