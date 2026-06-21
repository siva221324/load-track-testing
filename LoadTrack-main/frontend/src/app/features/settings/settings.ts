import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SettingsService } from '../../core/services/settings.service';
import { SettingsRequest } from '../../core/models/settings.model';

@Component({
  selector: 'app-settings',
  standalone: false,
  templateUrl: './settings.html',
  styleUrl: './settings.scss'
})
export class Settings implements OnInit {
  private fb = inject(FormBuilder);
  private settings = inject(SettingsService);
  private snack = inject(MatSnackBar);

  loading = signal(true);
  saving = signal(false);
  lastUpdated = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    interestRatePercent: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    allowedDays:         [30, [Validators.required, Validators.min(1)]]
  });

  ngOnInit(): void {
    this.settings.get().subscribe({
      next: (s) => {
        this.form.patchValue({
          interestRatePercent: s.interestRatePercent,
          allowedDays: s.allowedDays
        });
        this.lastUpdated.set(s.updatedAt ?? null);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load settings', 'Dismiss', { duration: 4000 });
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const payload = this.form.getRawValue() as SettingsRequest;
    this.settings.update(payload).subscribe({
      next: (s) => {
        this.saving.set(false);
        this.lastUpdated.set(s.updatedAt ?? null);
        this.snack.open('Settings saved', 'Dismiss', { duration: 3000 });
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Save failed', 'Dismiss', { duration: 4000 });
      }
    });
  }
}
