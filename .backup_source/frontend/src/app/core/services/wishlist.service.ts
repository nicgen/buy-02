import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class WishlistService {

    private apiUrl = `${environment.apiUrl}/api/wishlist`;

    constructor(private http: HttpClient) { }

    toggleWishlist(productId: string): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${productId}`, {});
    }

    getWishlist(): Observable<string[]> {
        return this.http.get<string[]>(`${this.apiUrl}`);
    }
}
