import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginForm } from '../components/form/LoginForm';
import { SignupForm } from '../components/form/SignupForm';
import { ForgotForm } from '../components/form/ForgotForm';

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type': 'application/json',
  }),
};

@Injectable({
  providedIn: 'root'
})

export class LoginService {
  private dbUrl = 'http://localhost:5000';

  constructor(private http: HttpClient) {}
  
  postUserLogin(newLogin: LoginForm):Observable<LoginForm>{
    return this.http.post<LoginForm>(`${this.dbUrl}/users`, newLogin);/**3.arg httpOptions*/
  }
  postUserSignup(newSignup: SignupForm):Observable<SignupForm>{
    return this.http.post<SignupForm>(`${this.dbUrl}/signup`, newSignup);/**3.arg httpOptions*/
  }
  postUserForgot(newForgot: ForgotForm):Observable<ForgotForm>{
    return this.http.post<ForgotForm>(`${this.dbUrl}/forgot`, newForgot);/**3.arg httpOptions*/
  }
  
}
