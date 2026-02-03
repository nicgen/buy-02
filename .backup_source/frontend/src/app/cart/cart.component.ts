import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService, CartItem } from '../core/services/cart.service';
import { AuthService } from '../core/services/auth.service';
import { OrderService } from '../core/services/order.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatRadioModule } from '@angular/material/radio';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
    selector: 'app-cart',
    standalone: true,
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatRadioModule,
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        ReactiveFormsModule,
        RouterLink
    ],
    templateUrl: './cart.component.html',
    styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
    cartItems: CartItem[] = [];
    total: number = 0;
    paymentMethod: string = 'PAY_ON_DELIVERY';
    addressForm: FormGroup;

    constructor(
        private cartService: CartService,
        private orderService: OrderService,
        private authService: AuthService,
        private router: Router,
        private fb: FormBuilder
    ) {
        this.addressForm = this.fb.group({
            street: ['', Validators.required],
            city: ['', Validators.required],
            zip: ['', Validators.required],
            country: ['', Validators.required],
            phoneNumber: ['']
        });
    }

    ngOnInit(): void {
        this.cartService.cartItems$.subscribe(items => {
            this.cartItems = items;
            this.total = this.cartService.getTotal();
        });

        if (this.authService.isLoggedIn()) {
            this.authService.getProfile().subscribe({
                next: (profile) => {
                    this.addressForm.patchValue({
                        street: profile.street,
                        city: profile.city,
                        zip: profile.zip,
                        country: profile.country,
                        phoneNumber: profile.phoneNumber
                    });
                },
                error: (err) => console.error('Could not load profile address', err)
            });
        }
    }

    removeFromCart(id: string): void {
        this.cartService.removeFromCart(id);
    }

    checkout(): void {
        if (this.cartItems.length === 0) return;

        if (!this.authService.isLoggedIn()) {
            alert('You must be logged in to place an order.');
            this.router.navigate(['/auth/login']);
            return;
        }

        if (this.addressForm.invalid) {
            alert('Please complete the shipping address.');
            this.addressForm.markAllAsTouched();
            return;
        }

        const orderItems = this.cartItems.map(item => ({
            productId: item.id,
            name: item.name,
            price: item.price,
            quantity: item.quantity,
            sellerId: 'unknown'
        }));

        const order: any = {
            items: orderItems,
            totalAmount: this.total,
            userId: this.authService.getUsername() || 'unknown',
            paymentMethod: this.paymentMethod,
            shippingAddress: this.addressForm.value,
            paymentDetails: {}
        };

        this.orderService.createOrder(order).subscribe({
            next: (res: any) => {
                if (this.paymentMethod === 'STRIPE' && res.paymentDetails && res.paymentDetails.stripeUrl) {
                    // Redirect to Stripe
                    window.location.href = res.paymentDetails.stripeUrl;
                } else {
                    alert('Order placed successfully!');
                    this.cartService.clearCart();
                    this.router.navigate(['/orders']);
                }
            },
            error: (err) => {
                console.error('Order failed', err);
                alert('Failed to place order. Please try again.');
            }
        });
    }
}
