import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../core/services/product.service';
import { WishlistService } from '../core/services/wishlist.service';
import { CartService } from '../core/services/cart.service';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-wishlist',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        RouterLink
    ],
    templateUrl: './wishlist.component.html',
    styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {
    products: any[] = [];
    wishlistIds: Set<string> = new Set();
    loading: boolean = true;

    constructor(
        private productService: ProductService,
        private wishlistService: WishlistService,
        private cartService: CartService
    ) { }

    ngOnInit(): void {
        this.loadWishlist();
    }

    loadWishlist(): void {
        this.wishlistService.getWishlist().subscribe({
            next: (ids: string[]) => {
                this.wishlistIds = new Set(ids);
                this.loadProducts();
            },
            error: (err: any) => {
                console.error('Failed to load wishlist', err);
                this.loading = false;
            }
        });
    }

    loadProducts(): void {
        this.productService.getAllProducts().subscribe({
            next: (allProducts: any[]) => {
                this.products = allProducts.filter(p => this.wishlistIds.has(p.id));
                this.loading = false;
            },
            error: (err: any) => {
                console.error('Failed to load products', err);
                this.loading = false;
            }
        });
    }

    removeFromWishlist(product: any): void {
        this.wishlistService.toggleWishlist(product.id).subscribe({
            next: () => {
                this.wishlistIds.delete(product.id);
                this.products = this.products.filter(p => p.id !== product.id);
            },
            error: (err: any) => console.error('Failed to remove from wishlist', err)
        });
    }

    addToCart(product: any): void {
        this.cartService.addToCart(product);
        // alert('Product added to cart!');
    }
}
