import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../core/services/product.service';
import { CartService } from '../core/services/cart.service';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';

import { WishlistService } from '../core/services/wishlist.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    RouterLink,
    MatInputModule,
    MatFormFieldModule,
    FormsModule,
    MatIconModule
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit {
  products: any[] = [];
  isLoggedIn: boolean = false;
  isSeller: boolean = false;
  searchQuery: string = '';
  minPrice?: number;
  maxPrice?: number;

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService
  ) { }

  wishlistIds: Set<string> = new Set();

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.isSeller = this.authService.getRole() === 'SELLER';
    this.loadProducts();
    if (this.isLoggedIn) {
      this.loadWishlist();
    }
  }

  loadWishlist(): void {
    this.wishlistService.getWishlist().subscribe({
      next: (ids: string[]) => this.wishlistIds = new Set(ids),
      error: (err: any) => console.error('Failed to load wishlist', err)
    });
  }

  loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: (data: any[]) => this.products = data,
      error: (err: any) => console.error('Failed to load products', err)
    });
  }

  onSearch(): void {

    if (this.searchQuery) {
      this.productService.searchProducts(this.searchQuery).subscribe({
        next: (data: any[]) => this.products = data,
        error: (err: any) => console.error('Search failed', err)
      });
    } else {

      this.loadProducts();
    }
  }

  onFilter(): void {

    this.productService.filterProducts(this.minPrice, this.maxPrice, this.searchQuery).subscribe({
      next: (data: any[]) => this.products = data,
      error: (err: any) => console.error('Filter failed', err)
    });
  }

  // Explicit binding handlers to fix ngModel issues
  onSearchChange(val: string): void {
    this.searchQuery = val;

  }

  onMinPriceChange(val: any): void {
    this.minPrice = val;
  }

  onMaxPriceChange(val: any): void {
    this.maxPrice = val;
  }

  onClearFilters(): void {
    this.searchQuery = '';
    this.minPrice = undefined;
    this.maxPrice = undefined;
    this.loadProducts();
  }

  addToCart(product: any): void {
    this.cartService.addToCart(product);
  }

  toggleWishlist(product: any): void {
    this.wishlistService.toggleWishlist(product.id).subscribe({
      next: () => {
        if (this.wishlistIds.has(product.id)) {
          this.wishlistIds.delete(product.id);
        } else {
          this.wishlistIds.add(product.id);
        }
      },
      error: (err: any) => console.error('Failed to toggle wishlist', err)
    });
  }

  isInWishlist(product: any): boolean {
    return this.wishlistIds.has(product.id);
  }
}
