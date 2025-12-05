// src/app/core/auth/auth.service.spec.ts

import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { AuthService, LoginResponse } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const API_URL = `${environment.apiBaseUrl}/auth`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería crearse', () => {
    expect(service).toBeTruthy();
  });

  it('debería llamar a /auth/login y guardar sesión', () => {
    const mockRes: LoginResponse = {
      email: 'test@example.com',
      token: 'fake-token',
      id: 1,
      message: 'OK',
      roles: ['USER'],
      fullName: 'Test User',
    };

    service.login('test@example.com', '123456').subscribe((res) => {
      expect(res).toEqual(mockRes);
    });

    const req = httpMock.expectOne(`${API_URL}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'test@example.com',
      password: '123456',
    });

    req.flush(mockRes);

    expect(localStorage.getItem('token')).toBe('fake-token');
    expect(localStorage.getItem('email')).toBe('test@example.com');
    expect(localStorage.getItem('userId')).toBe('1');
    expect(localStorage.getItem('fullName')).toBe('Test User');
  });

  it('debería llamar a /auth/register y guardar sesión', () => {
    const mockRes: LoginResponse = {
      email: 'new@example.com',
      token: 'token123',
      id: 99,
      message: 'OK',
      roles: ['USER'],
      fullName: 'Nuevo Usuario',
    };

    service.register('new@example.com', '123456', 'Nuevo Usuario')
      .subscribe((res: LoginResponse) => {
        expect(res).toEqual(mockRes);
      });


    const req = httpMock.expectOne(`${API_URL}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'new@example.com',
      password: '123456',
      fullName: 'Nuevo Usuario',
    });

    req.flush(mockRes);

    expect(localStorage.getItem('token')).toBe('token123');
    expect(localStorage.getItem('email')).toBe('new@example.com');
    expect(localStorage.getItem('userId')).toBe('99');
  });

  it('debería limpiar el localStorage al hacer logout', () => {
    localStorage.setItem('token', '123');
    localStorage.setItem('email', 'a@b.com');

    service.logout();

    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('email')).toBeNull();
  });
});
