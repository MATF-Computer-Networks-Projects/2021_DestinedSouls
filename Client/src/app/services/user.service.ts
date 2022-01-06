import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent} from '@angular/common/http';
import {BehaviorSubject, Observable, of} from 'rxjs';
import { map } from 'rxjs/operators';

import { LoggedUser, User } from 'src/app/models'
import {Swipe} from "../models/swipe";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {
  }

  register(user: User): Observable<LoggedUser> {
    return this.http.post<LoggedUser>('/users/register', user);
  }

  getSwipes(): Observable<User[]> {
    return this.http.get<User[]>('/users/swipes');
  }

  sendSwipeVotes(votes: Swipe[]) {
    return this.http.post('/users/swipes', votes);
  }

  upload(file: File, token: string): Observable<any> {
    const formData = new FormData();
    formData.append("thumbnail", file);

    return this.http.post("/upload", formData, {
      headers: { Authorization: `Bearer ${token}` },
      reportProgress: true,
      observe: 'events'
    });
  }
}
