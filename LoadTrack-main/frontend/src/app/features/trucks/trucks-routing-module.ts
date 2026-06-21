import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TruckList } from './truck-list';

const routes: Routes = [
  { path: '', component: TruckList }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TrucksRoutingModule { }
