import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent} from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { User } from 'src/app/models/user'

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  register(user: User): Observable<User> {
    return this.http.post<User>('/users/register', user);
  }

  getAll(): Observable<User[]> {
    return this.http.get<User[]>('/users/getAll');
  }

  upload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append("thumbnail", file);

    return this.http.post("/upload", formData, {
      reportProgress: true,
      observe: 'events'
    });
  }
}
