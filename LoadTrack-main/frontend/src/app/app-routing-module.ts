import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { LayoutShell } from './shared/layout/layout-shell';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: '/app/home' },
  {
    path: 'login',
    loadChildren: () => import('./features/auth/auth-module').then(m => m.AuthModule)
  },
  {
    path: 'app',
    canActivate: [authGuard],
    component: LayoutShell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      {
        path: 'home',
        loadChildren: () => import('./features/home/home-module').then(m => m.HomeModule)
      },
      {
        path: 'trucks',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/trucks/trucks-module').then(m => m.TrucksModule)
      },
      {
        path: 'drivers',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/drivers/drivers-module').then(m => m.DriversModule)
      },
      {
        path: 'dealers',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/dealers/dealers-module').then(m => m.DealersModule)
      },
      {
        path: 'sand-types',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/sand-types/sand-types-module').then(m => m.SandTypesModule)
      },
      {
        path: 'trips',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/trips/trips-module').then(m => m.TripsModule)
      },
      {
        path: 'payments',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/payments/payments-module').then(m => m.PaymentsModule)
      },
      {
        path: 'settings',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/settings/settings-module').then(m => m.SettingsModule)
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/reports/reports-module').then(m => m.ReportsModule)
      },
      {
        path: 'driver',
        canActivate: [roleGuard],
        data: { roles: ['DRIVER'] },
        loadChildren: () => import('./features/driver-portal/driver-portal-module').then(m => m.DriverPortalModule)
      },
      {
        path: 'dealer',
        canActivate: [roleGuard],
        data: { roles: ['DEALER'] },
        loadChildren: () => import('./features/dealer-portal/dealer-portal-module').then(m => m.DealerPortalModule)
      },
      {
        path: 'account',
        loadChildren: () => import('./features/account/account-module').then(m => m.AccountModule)
      },
      {
        path: 'trip-requests',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/trip-requests/trip-requests-module').then(m => m.TripRequestsModule)
      },
      {
        path: 'dealer-requests',
        canActivate: [roleGuard],
        data: { roles: ['DEALER'] },
        loadChildren: () => import('./features/dealer-requests/dealer-requests-module').then(m => m.DealerRequestsModule)
      }
    ]
  },
  { path: '**', redirectTo: '/app/home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
