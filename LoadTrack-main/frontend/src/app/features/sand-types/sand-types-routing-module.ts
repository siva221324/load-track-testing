import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SandTypeList } from './sand-type-list';

const routes: Routes = [
  { path: '', component: SandTypeList }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SandTypesRoutingModule { }
