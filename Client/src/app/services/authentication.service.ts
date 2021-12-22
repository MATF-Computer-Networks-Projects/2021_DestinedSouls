import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;

  constructor(private http: HttpClient) {
      this.currentUserSubject = new BehaviorSubject<User>(JSON.parse( localStorage.getItem('currentUser') ));
      this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
      return this.currentUserSubject.value;
  }

  login(email : string, password : string) {
      return this.http.post<any>('/users/authenticate', { email, password })
          .pipe(map(user => {
              // store user details and jwt token in local storage to keep user logged in between page refreshes
              localStorage.setItem('currentUser', JSON.stringify(user));
              this.currentUserSubject.next(user);
              return user;
          }));
  }

  logout() {
    // remove user from local storage and set current user to null
    //TODO: reroute to '/loign'
    localStorage.removeItem('currentUser');
    return this.http.delete(`/users/${this.currentUserSubject.getValue().id}`)
      .pipe(map(data =>{this.currentUserSubject.next(null);}));
  }
  // logout() {
//   //FIXME: ovo iz nekog razloga ucini da delete metod ceka
//   localStorage.removeItem('currentUser');
//   let url: string;
//   url = "/users/" + `${this.currentUserSubject.getValue().id}`;
//   this.currentUserSubject.next(null);
//   console.log("currentUserSubject.id=" + url)
//   return this.http.delete(url);
// }
}

