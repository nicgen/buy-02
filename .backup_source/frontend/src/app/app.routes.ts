import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { SellerDashboardComponent } from './seller-dashboard/seller-dashboard.component';
import { ProductListComponent } from './product-list/product-list.component';
import { CartComponent } from './cart/cart.component';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    { path: 'auth/login', component: LoginComponent },
    { path: 'auth/register', component: RegisterComponent },
    { path: 'seller-dashboard', component: SellerDashboardComponent, canActivate: [AuthGuard] },
    { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
    { path: 'orders', loadComponent: () => import('./order-history/order-history.component').then(m => m.OrderHistoryComponent), canActivate: [AuthGuard] },
    { path: 'profile', loadComponent: () => import('./user-profile/user-profile.component').then(m => m.UserProfileComponent), canActivate: [AuthGuard] },
    { path: 'wishlist', loadComponent: () => import('./wishlist/wishlist.component').then(m => m.WishlistComponent), canActivate: [AuthGuard] },
    { path: 'products', component: ProductListComponent },
    { path: '', redirectTo: '/products', pathMatch: 'full' }
];
