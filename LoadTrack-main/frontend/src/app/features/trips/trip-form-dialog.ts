import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { TripService } from '../../core/services/trip.service';
import { TruckService } from '../../core/services/truck.service';
import { DriverService } from '../../core/services/driver.service';
import { DealerService } from '../../core/services/dealer.service';
import { SandTypeService } from '../../core/services/sand-type.service';
import { Trip, TripRequest } from '../../core/models/trip.model';
import { Truck } from '../../core/models/truck.model';
import { Driver } from '../../core/models/driver.model';
import { Dealer } from '../../core/models/dealer.model';
import { SandType } from '../../core/models/sand-type.model';

export interface TripFormDialogData {
  mode: 'add' | 'edit';
  trip?: Trip;
}

@Component({
  selector: 'app-trip-form-dialog',
  standalone: false,
  templateUrl: './trip-form-dialog.html',
  styleUrl: './trip-form-dialog.scss'
})
export class TripFormDialog implements OnInit {
  private fb = inject(FormBuilder);
  private trips = inject(TripService);
  private trucksApi = inject(TruckService);
  private driversApi = inject(DriverService);
  private dealersApi = inject(DealerService);
  private sandTypesApi = inject(SandTypeService);
  private snack = inject(MatSnackBar);

  public dialogRef = inject<MatDialogRef<TripFormDialog, Trip | null>>(MatDialogRef);
  public data = inject<TripFormDialogData>(MAT_DIALOG_DATA);

  loading = signal(true);
  saving = signal(false);

  trucks = signal<Truck[]>([]);
  drivers = signal<Driver[]>([]);
  dealers = signal<Dealer[]>([]);
  sandTypes = signal<SandType[]>([]);

  isEdit = this.data.mode === 'edit';

  form = this.fb.nonNullable.group({
    truckId:              this.fb.control<number | null>(this.data.trip?.truck.id ?? null,    Validators.required),
    driverId:             this.fb.control<number | null>(this.data.trip?.driver.id ?? null,   Validators.required),
    dealerId:             this.fb.control<number | null>(this.data.trip?.dealer.id ?? null,   Validators.required),
    sandTypeId:           this.fb.control<number | null>(this.data.trip?.sandType.id ?? null, Validators.required),
    tons:                 [this.data.trip?.tons ?? 0,                 [Validators.required, Validators.min(0.01)]],
    sourceLocation:       [this.data.trip?.sourceLocation ?? '',      [Validators.required, Validators.maxLength(200)]],
    destinationLocation:  [this.data.trip?.destinationLocation ?? '', [Validators.required, Validators.maxLength(200)]],
    tripDate:             [this.data.trip?.tripDate ?? this.todayIso(), Validators.required]
  });

  // Live calculation
  selectedSandType = signal<SandType | null>(null);
  ratePerTon = computed(() => this.selectedSandType()?.pricePerTon ?? 0);

  ngOnInit(): void {
    // For new trips, only show AVAILABLE trucks. For edit, show all so the current ON_TRIP truck appears.
    const truckObs = this.isEdit
      ? this.trucksApi.list({ size: 200, sort: 'truckNumber,asc' })
      : this.trucksApi.list({ size: 200, sort: 'truckNumber,asc', status: 'AVAILABLE' });

    forkJoin({
      trucks:    truckObs,
      drivers:   this.driversApi.list({ size: 200, sort: 'name,asc' }),
      dealers:   this.dealersApi.list({ size: 200, sort: 'name,asc' }),
      sandTypes: this.sandTypesApi.list()
    }).subscribe({
      next: (res) => {
        this.trucks.set(res.trucks.content);
        this.drivers.set(res.drivers.content);
        this.dealers.set(res.dealers.content);
        this.sandTypes.set(res.sandTypes);

        // Initialize selected sand type for live calc
        const initialSandTypeId = this.form.controls.sandTypeId.value;
        if (initialSandTypeId !== null) {
          this.selectedSandType.set(res.sandTypes.find(s => s.id === initialSandTypeId) ?? null);
        }

        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load form data', 'Dismiss', { duration: 4000 });
      }
    });

    // Live update of selected sand type when dropdown changes
    this.form.controls.sandTypeId.valueChanges.subscribe((id) => {
      this.selectedSandType.set(this.sandTypes().find(s => s.id === id) ?? null);
    });
  }

  totalAmount = computed(() => {
    const tons = this.form.controls.tons.value || 0;
    return Number(tons) * this.ratePerTon();
  });

  // Recompute when tons changes (signal alone doesn't react to form changes, so wire it)
  ngAfterContentInit() { /* no-op; the form is already wired */ }

  save(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    const v = this.form.getRawValue();
    const payload: TripRequest = {
      truckId: v.truckId!,
      driverId: v.driverId!,
      dealerId: v.dealerId!,
      sandTypeId: v.sandTypeId!,
      tons: Number(v.tons),
      sourceLocation: v.sourceLocation,
      destinationLocation: v.destinationLocation,
      tripDate: v.tripDate
    };

    const obs = this.isEdit
      ? this.trips.update(this.data.trip!.id, payload)
      : this.trips.create(payload);

    obs.subscribe({
      next: (trip) => {
        this.snack.open(this.isEdit ? 'Trip updated' : 'Trip created', 'Dismiss', { duration: 3000 });
        this.dialogRef.close(trip);
      },
      error: (err) => {
        this.saving.set(false);
        this.snack.open(err?.error?.message ?? 'Save failed', 'Dismiss', { duration: 5000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  // For the live total calc to react to tons input we need a getter the template can call
  get currentTotal(): number {
    const tons = Number(this.form.controls.tons.value) || 0;
    const rate = this.selectedSandType()?.pricePerTon ?? 0;
    return tons * rate;
  }

  private todayIso(): string {
    return new Date().toISOString().substring(0, 10);
  }
}
