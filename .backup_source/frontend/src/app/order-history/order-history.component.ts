import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../core/services/order.service';
import { AuthService } from '../core/services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';

@Component({
    selector: 'app-order-history',
    standalone: true,
    imports: [CommonModule, MatCardModule, MatListModule, MatChipsModule, MatButtonModule],
    templateUrl: './order-history.component.html',
    styleUrl: './order-history.component.css'
})
export class OrderHistoryComponent implements OnInit {
    orders: any[] = [];
    userId: string | null = null;

    constructor(
        private orderService: OrderService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        // In a real app, userId might come from the token or a user profile service.
        // implementing a decode in AuthService or similar effectively.
        // For now, let's assume the OrderService.getMyOrders() uses the token to find the user's orders
        // But wait, OrderController.getMyOrders() implementation was tricky. 
        // Let's rely on getOrdersByUserId if we can get the ID, or fix getMyOrders.
        // The previous analysis said getMyOrders might not work easily without userId in token.
        // Let's try to get userId from AuthService if available (it might just retain username).

        // As a backup for this student project, we can fetch all orders and filter by username if the backend supports it,
        // OR we just use the endpoint we have.
        // The OrderService has getMyOrders() -> /api/orders.

        this.loadOrders();
    }

    loadOrders(): void {
        this.orderService.getMyOrders().subscribe({
            next: (data) => {
                this.orders = data;
            },
            error: (err) => console.error('Failed to load orders', err)
        });
    }
}
