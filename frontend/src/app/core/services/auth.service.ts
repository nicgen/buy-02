import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api/auth`;
  private tokenKey = 'auth_token';
  private roleKey = 'user_role';
  private usernameKey = 'auth_username';
  private roleSubject = new BehaviorSubject<string | null>(this.getRole());
  public role$ = this.roleSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) { }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user).pipe(
      tap((response: any) => {
        this.handleAuthResponse(response);
      })
    );
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((response: any) => {
        this.handleAuthResponse(response);
      })
    );
  }

  private handleAuthResponse(response: any): void {
    if (response.token) {
      localStorage.setItem(this.tokenKey, response.token);
      localStorage.setItem(this.roleKey, response.role);
      if (response.username) {
        localStorage.setItem(this.usernameKey, response.username);
      }
      this.roleSubject.next(response.role);
    }
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.usernameKey);
    this.roleSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.usernameKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/profile`);
  }

  updateProfile(profileData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/profile`, profileData);
  }
}
