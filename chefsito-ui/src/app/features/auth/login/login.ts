// src/app/features/auth/login/login.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class LoginComponent {
  usernameOrEmail = '';
  password = '';
  loading = false;
  error: string | null = null;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  onSubmit(form: NgForm): void {
    if (form.invalid || this.loading) {
      return;
    }

    this.error = null;
    this.loading = true;

    this.authService.login(this.usernameOrEmail, this.password).subscribe({
      next: () => {
        this.loading = false;
        // Redirigimos a la vista de despensa
        this.router.navigate(['/despensa']);
      },
      error: (err) => {
        console.error('Error en login', err);
        this.loading = false;

        // Mensaje amigable
        if (err?.status === 401) {
          this.error = 'Credenciales incorrectas';
        } else {
          this.error = 'Error al iniciar sesión. Inténtalo de nuevo.';
        }
      },
    });
  }
}
