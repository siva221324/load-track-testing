import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';

import { DriverPortalRoutingModule } from './driver-portal-routing-module';
import { DriverDashboard } from './driver-dashboard';

@NgModule({
  declarations: [DriverDashboard],
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatButtonToggleModule,
    MatIconModule,
    DriverPortalRoutingModule
  ]
})
export class DriverPortalModule { }
