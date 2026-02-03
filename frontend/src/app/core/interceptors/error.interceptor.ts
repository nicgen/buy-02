import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 || error.status === 403) {
                // Auto logout if 401/403 response returned from API
                authService.logout();
                router.navigate(['/login']);
            }
            return throwError(() => error);
        })
    );
};
