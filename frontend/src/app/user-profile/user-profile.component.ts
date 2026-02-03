import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../core/services/auth.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
    selector: 'app-user-profile',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatCardModule
    ],
    templateUrl: './user-profile.component.html',
    styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
    infoForm: FormGroup;
    passwordForm: FormGroup;
    userEmail: string = '';
    successMessage: string = '';
    errorMessage: string = '';
    passwordSuccessMessage: string = '';
    passwordErrorMessage: string = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService
    ) {
        this.infoForm = this.fb.group({
            street: [''],
            city: [''],
            zip: [''],
            country: [''],
            phoneNumber: ['']
        });

        this.passwordForm = this.fb.group({
            password: ['', [Validators.required, Validators.minLength(6)]]
        });
    }

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.authService.getProfile().subscribe({
            next: (data: any) => {
                this.userEmail = data.email;
                this.infoForm.patchValue({
                    street: data.street,
                    city: data.city,
                    zip: data.zip,
                    country: data.country,
                    phoneNumber: data.phoneNumber
                });
            },
            error: (err: any) => {
                console.error('Failed to load profile', err);
                this.errorMessage = 'Failed to load profile data.';
            }
        });
    }

    onInfoSubmit(): void {
        if (this.infoForm.valid) {
            this.authService.updateProfile(this.infoForm.getRawValue()).subscribe({
                next: () => {
                    this.successMessage = 'Details updated successfully!';
                    this.errorMessage = '';
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: (err: any) => {
                    console.error('Update failed', err);
                    this.errorMessage = 'Failed to update details.';
                    this.successMessage = '';
                }
            });
        }
    }

    onPasswordSubmit(): void {
        if (this.passwordForm.valid) {
            const payload = { password: this.passwordForm.value.password };
            this.authService.updateProfile(payload).subscribe({
                next: () => {
                    this.passwordSuccessMessage = 'Password changed successfully!';
                    this.passwordErrorMessage = '';
                    this.passwordForm.reset();
                    setTimeout(() => this.passwordSuccessMessage = '', 3000);
                },
                error: (err: any) => {
                    console.error('Password update failed', err);
                    this.passwordErrorMessage = 'Failed to update password.';
                    this.passwordSuccessMessage = '';
                }
            });
        }
    }
}
