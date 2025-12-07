// src/app/features/admin.ts

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminUser } from './admin.service';
import { AuthService } from '../core/auth/auth.service'; // ← ruta corregida

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.html',
  styleUrls: ['./admin.scss'], // ← propiedad correcta (plural)
})
export class Admin implements OnInit {
  private adminService = inject(AdminService);
  private authService = inject(AuthService);

  users: AdminUser[] = [];
  loading = false;
  error: string | null = null;
  totalUsers: number | null = null;

  ngOnInit(): void {
    this.loadUsers();
    this.loadStats();
  }

  private loadUsers(): void {
    this.loading = true;
    this.error = null;

    this.adminService.getUsers().subscribe({
      next: (users) => {
        // Mostrar TODOS los usuarios, sin filtros
        this.users = users;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error al cargar los usuarios';
        this.loading = false;
      },
    });
  }

  private loadStats(): void {
    this.adminService.getStats().subscribe({
      next: (stats) => {
        this.totalUsers = stats.totalUsers;
      },
      error: () => {
        this.totalUsers = null;
      },
    });
  }

  // Comprueba si el usuario logueado es el mismo que el de la fila
  isSelf(user: AdminUser): boolean {
    const myId = this.authService.getUserId
      ? this.authService.getUserId()
      : null;
    return myId !== null && user.id === myId;
  }

  // Eliminar usuario (única acción del panel)
  deleteUser(user: AdminUser): void {
    if (this.isSelf(user)) {
      alert('No puedes borrarte a ti mismo 😂');
      return;
    }

    const ok = confirm(
      `¿Seguro que quieres eliminar al usuario ${user.email}? Esta acción no se puede deshacer.`
    );
    if (!ok) {
      return;
    }

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter((u) => u.id !== user.id);
      },
      error: () => {
        alert('Error al eliminar el usuario');
      },
    });
  }
}
