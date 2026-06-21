import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { SandTypeService } from '../../core/services/sand-type.service';
import { TripRequestService } from '../../core/services/trip-request.service';
import { CreateTripRequestPayload, TripRequest } from '../../core/models/trip-request.model';
import { SandType } from '../../core/models/sand-type.model';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/dialogs/confirm-dialog';

@Component({
  selector: 'app-dealer-requests',
  standalone: false,
  templateUrl: './dealer-requests.html',
  styleUrl: './dealer-requests.scss'
})
export class DealerRequests implements OnInit {
  private fb = inject(FormBuilder);
  private sandTypesApi = inject(SandTypeService);
  private requestsApi = inject(TripRequestService);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  loading = signal(true);
  submitting = signal(false);
  sandTypes = signal<SandType[]>([]);
  requests = signal<TripRequest[]>([]);

  displayedColumns = ['id', 'requestedDate', 'sandType', 'tons', 'estimatedAmount',
                      'route', 'status', 'actions'];

  form = this.fb.nonNullable.group({
    sandTypeId: this.fb.control<number | null>(null, Validators.required),
    tons: [0, [Validators.required, Validators.min(0.01)]],
    sourceLocation: ['', [Validators.required, Validators.maxLength(200)]],
    destinationLocation: ['', [Validators.required, Validators.maxLength(200)]],
    requestedDate: [this.todayIso(), Validators.required],
    notes: ['']
  });

  selectedSandType = signal<SandType | null>(null);
  estimatedTotal = computed(() => {
    const sandType = this.selectedSandType();
    const tons = Number(this.form.controls.tons.value) || 0;
    return sandType ? tons * sandType.pricePerTon : 0;
  });

  ngOnInit(): void {
    this.sandTypesApi.list().subscribe({
      next: (rows) => this.sandTypes.set(rows),
      error: (err) => this.snack.open(err?.error?.message ?? 'Failed to load sand types', 'Dismiss', { duration: 4000 })
    });
    this.loadRequests();

    this.form.controls.sandTypeId.valueChanges.subscribe((id) => {
      this.selectedSandType.set(this.sandTypes().find(s => s.id === id) ?? null);
    });
  }

  loadRequests(): void {
    this.loading.set(true);
    this.requestsApi.listForDealer(0, 100).subscribe({
      next: (page) => { this.requests.set(page.content); this.loading.set(false); },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to load requests', 'Dismiss', { duration: 4000 });
      }
    });
  }

  submit(): void {
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    const v = this.form.getRawValue();
    const payload: CreateTripRequestPayload = {
      sandTypeId: v.sandTypeId!,
      tons: Number(v.tons),
      sourceLocation: v.sourceLocation,
      destinationLocation: v.destinationLocation,
      requestedDate: v.requestedDate,
      notes: v.notes || undefined
    };

    this.requestsApi.createByDealer(payload).subscribe({
      next: () => {
        this.submitting.set(false);
        this.snack.open('Trip request submitted. Admin will review shortly.', 'Dismiss', { duration: 4000 });
        this.form.reset({
          sandTypeId: null,
          tons: 0,
          sourceLocation: '',
          destinationLocation: '',
          requestedDate: this.todayIso(),
          notes: ''
        });
        this.selectedSandType.set(null);
        this.loadRequests();
      },
      error: (err) => {
        this.submitting.set(false);
        this.snack.open(err?.error?.message ?? 'Failed to submit request', 'Dismiss', { duration: 5000 });
      }
    });
  }

  cancel(req: TripRequest): void {
    if (req.status !== 'PENDING') return;
    const data: ConfirmDialogData = {
      title: 'Cancel Request',
      message: `Cancel your request for ${req.tons} tons of ${req.sandType.name}?`,
      confirmText: 'Yes, cancel',
      cancelText: 'Keep it',
      danger: true
    };
    this.dialog.open(ConfirmDialog, { data }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.requestsApi.cancelByDealer(req.id).subscribe({
        next: () => {
          this.snack.open('Request cancelled', 'Dismiss', { duration: 3000 });
          this.loadRequests();
        },
        error: (err) => this.snack.open(err?.error?.message ?? 'Cancel failed', 'Dismiss', { duration: 4000 })
      });
    });
  }

  protected todayIso(): string {
    return new Date().toISOString().substring(0, 10);
  }
}
