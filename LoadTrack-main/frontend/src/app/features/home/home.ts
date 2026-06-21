import { Component, OnInit, inject, signal } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { AdminDashboard } from '../../core/models/admin-dashboard.model';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  protected auth = inject(AuthService);
  private dashboard = inject(DashboardService);
  private snack = inject(MatSnackBar);

  loading = signal(true);
  data = signal<AdminDashboard | null>(null);

  earningsChartData: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [] };
  earningsChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, ticks: { callback: (v) => '₹' + (v as number).toLocaleString() } }
    }
  };

  tripsChartData: ChartConfiguration<'doughnut'>['data'] = { labels: [], datasets: [] };
  tripsChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'bottom' } }
  };

  ngOnInit(): void {
    this.dashboard.adminSummary().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
        this.buildCharts(d);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load dashboard', 'Dismiss', { duration: 4000 });
      }
    });
  }

  private buildCharts(d: AdminDashboard): void {
    this.earningsChartData = {
      labels: d.monthlyEarnings.map(m => m.label),
      datasets: [{
        data: d.monthlyEarnings.map(m => m.amount),
        label: 'Monthly Earnings (₹)',
        backgroundColor: '#1976d2',
        borderRadius: 4
      }]
    };
    this.tripsChartData = {
      labels: ['Pending', 'Active', 'Completed'],
      datasets: [{
        data: [d.pendingTrips, d.activeTrips, d.completedTrips],
        backgroundColor: ['#ff9800', '#2196f3', '#4caf50'],
        borderWidth: 0
      }]
    };
  }
}
