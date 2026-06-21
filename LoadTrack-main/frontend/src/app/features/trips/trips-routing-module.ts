import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TripList } from './trip-list';

const routes: Routes = [
  { path: '', component: TripList }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TripsRoutingModule { }
