import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
}
