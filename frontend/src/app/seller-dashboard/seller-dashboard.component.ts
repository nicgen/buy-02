import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../core/services/product.service';
import { OrderService } from '../core/services/order.service';
import { MediaService } from '../core/services/media.service';
import { AuthService } from '../core/services/auth.service';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-seller-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatListModule,
    MatIconModule
  ],
  templateUrl: './seller-dashboard.component.html',
  styleUrl: './seller-dashboard.component.css'
})
export class SellerDashboardComponent implements OnInit {
  products: any[] = [];
  productForm: FormGroup;
  selectedFile: File | null = null;
  errorMessage: string = '';
  stats: any = null;

  constructor(
    private productService: ProductService,
    private mediaService: MediaService,
    private authService: AuthService,
    private orderService: OrderService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    if (this.authService.getRole() !== 'SELLER') {
      this.router.navigate(['/products']);
      return;
    }
    this.loadProducts();
    this.loadStats();
  }

  loadStats(): void {
    const username = this.authService.getUsername();
    if (username) {
      this.orderService.getSellerStats(username).subscribe({
        next: (data) => this.stats = data,
        error: (err) => console.error('Failed to load stats', err)
      });
    }
  }

  loadProducts(): void {
    this.productService.getSellerProducts().subscribe({
      next: (data: any[]) => this.products = data,
      error: (err: any) => console.error('Failed to load products', err)
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
  }

  onSubmit(): void {
    if (this.productForm.valid) {
      const productData = this.productForm.value;
      productData.imageUrls = [];

      if (this.selectedFile) {
        this.mediaService.uploadMedia(this.selectedFile).subscribe({
          next: (media: any) => {
            productData.imageUrls.push(`${environment.apiUrl}/api/media/${media.id}`);
            this.createProduct(productData);
          },
          error: (err: any) => {
            this.errorMessage = 'Failed to upload image';
            console.error(err);
          }
        });
      } else {
        this.createProduct(productData);
      }
    }
  }

  createProduct(productData: any): void {
    this.productService.createProduct(productData).subscribe({
      next: () => {
        this.loadProducts();
        this.productForm.reset();
        this.selectedFile = null;
      },
      error: (err: any) => {
        this.errorMessage = 'Failed to create product';
        console.error(err);
      }
    });
  }

  deleteProduct(id: string): void {
    this.productService.deleteProduct(id).subscribe({
      next: () => this.loadProducts(),
      error: (err: any) => console.error('Failed to delete product', err)
    });
  }


}
