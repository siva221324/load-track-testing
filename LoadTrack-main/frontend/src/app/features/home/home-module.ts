import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';

import { HomeRoutingModule } from './home-routing-module';
import { Home } from './home';

@NgModule({
  declarations: [Home],
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
    BaseChartDirective,
    HomeRoutingModule
  ],
  providers: [
    provideCharts(withDefaultRegisterables())
  ]
})
export class HomeModule { }
