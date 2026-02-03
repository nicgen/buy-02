import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CartItem {
    id: string;
    name: string;
    price: number;
    quantity: number;
    imageUrl?: string;
}

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private cartItems = new BehaviorSubject<CartItem[]>([]);
    cartItems$ = this.cartItems.asObservable();

    constructor() {
        this.loadCart();
    }

    private loadCart() {
        const savedCart = localStorage.getItem('cart');
        if (savedCart) {
            this.cartItems.next(JSON.parse(savedCart));
        }
    }

    private saveCart(items: CartItem[]) {
        localStorage.setItem('cart', JSON.stringify(items));
        this.cartItems.next(items);
    }

    addToCart(product: any) {
        const currentItems = this.cartItems.value;
        const existingItem = currentItems.find(item => item.id === product.id);

        if (existingItem) {
            existingItem.quantity += 1;
            this.saveCart([...currentItems]);
        } else {
            const newItem: CartItem = {
                id: product.id,
                name: product.name,
                price: product.price,
                quantity: 1,
                imageUrl: product.imageUrls && product.imageUrls.length > 0 ? product.imageUrls[0] : undefined
            };
            this.saveCart([...currentItems, newItem]);
        }
    }

    removeFromCart(productId: string) {
        const currentItems = this.cartItems.value.filter(item => item.id !== productId);
        this.saveCart(currentItems);
    }

    clearCart() {
        this.saveCart([]);
    }

    getTotal(): number {
        return this.cartItems.value.reduce((acc, item) => acc + (item.price * item.quantity), 0);
    }
}
