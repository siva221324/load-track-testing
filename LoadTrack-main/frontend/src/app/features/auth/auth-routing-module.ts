import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './login';
import { Signup } from './signup';
import { ForgotPassword } from './forgot-password';

const routes: Routes = [
  { path: '', component: Login },
  { path: 'signup', component: Signup },
  { path: 'forgot-password', component: ForgotPassword }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule { }
