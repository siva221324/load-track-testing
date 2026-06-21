import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PaymentList } from './payment-list';

const routes: Routes = [
  { path: '', component: PaymentList }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PaymentsRoutingModule { }
