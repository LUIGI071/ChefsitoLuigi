// src/app/features/admin.ts

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminUser } from './admin.service';
import { AuthService } from '../core/auth/auth.service'; // ajusta ruta si en tu proyecto cambia

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
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

  isAdminRole(user: AdminUser): boolean {
    return (user.roles || []).includes('ROLE_ADMIN');
  }

  isSelf(user: AdminUser): boolean {
    const myId = this.authService.getUserId
      ? this.authService.getUserId()
      : null;
    return myId !== null && user.id === myId;
  }

  deleteUser(user: AdminUser): void {
    if (this.isSelf(user)) {
      alert('No puedes borrarte a ti mismo 😂');
      return;
    }

    const ok = confirm(
      `¿Seguro que quieres eliminar al usuario ${user.email}? Esta acción no se puede deshacer.`
    );
    if (!ok) return;

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter((u) => u.id !== user.id);
      },
      error: () => {
        alert('Error al eliminar el usuario');
      },
    });
  }

  toggleAdmin(user: AdminUser): void {
    if (this.isSelf(user)) {
      alert('No tiene sentido quitarte tu propio rol aquí 🙃');
      return;
    }

    const rolesSet = new Set(user.roles || []);

    if (rolesSet.has('ROLE_ADMIN')) {
      rolesSet.delete('ROLE_ADMIN');
    } else {
      rolesSet.add('ROLE_ADMIN');
      rolesSet.add('ROLE_USER');
    }

    const newRoles = Array.from(rolesSet);

    this.adminService.updateUserRoles(user.id, newRoles).subscribe({
      next: (updated) => {
        this.users = this.users.map((u) =>
          u.id === updated.id ? { ...u, roles: updated.roles } : u
        );
      },
      error: () => {
        alert('Error al actualizar los roles del usuario');
      },
    });
  }
}
