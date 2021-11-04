import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginForm } from '../components/form/Form';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root'
})

export class LoginService {
  private dbUrl = 'http://localhost:5000/users';

  constructor(private http: HttpClient) {}
  
  postUser(newLogin: LoginForm):Observable<LoginForm>{
    return this.http.post<LoginForm>(this.dbUrl, newLogin);/**3.arg httpOptions*/
  }
  
}
