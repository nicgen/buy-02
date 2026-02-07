import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) { }

  getAllProducts(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getSellerProducts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/seller`);
  }

  createProduct(product: any): Observable<any> {
    return this.http.post(this.apiUrl, product);
  }

  deleteProduct(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  searchProducts(query: string): Observable<any[]> {
    if (!query) {
      return this.getAllProducts();
    }
    const params = new HttpParams().set('query', query);
    return this.http.get<any[]>(`${this.apiUrl}/search`, { params });
  }

  filterProducts(minPrice?: number, maxPrice?: number, query?: string): Observable<any[]> {
    let params = new HttpParams();
    if (minPrice !== undefined && minPrice !== null) params = params.set('minPrice', minPrice.toString());
    if (maxPrice !== undefined && maxPrice !== null) params = params.set('maxPrice', maxPrice.toString());
    if (query) params = params.set('query', query);
    return this.http.get<any[]>(`${this.apiUrl}/filter`, { params });
  }
} // End of class. Note: Need to import HttpParams.
