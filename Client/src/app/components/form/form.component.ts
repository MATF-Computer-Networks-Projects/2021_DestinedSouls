import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { LoginForm } from './LoginForm';
import { SignupForm } from './SignupForm';
import { ForgotForm } from './ForgotForm';
import { Observable, throwError, of } from 'rxjs';
import { catchError, retry} from 'rxjs/operators';
import { LoginService } from 'src/app/services/login.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {
  @Output() onLogin : EventEmitter<LoginForm> = new EventEmitter();
  /*onLoginSubmit --> onLogin*/
  @Output() onSignup : EventEmitter<SignupForm> = new EventEmitter();
  @Output() onForgot : EventEmitter<ForgotForm> = new EventEmitter();

  name : string | any;
  birthdate: string | any;
  gender : string | any;
  interest: string | any;
  email: string | any;
  password: string | any;

  showLogin:boolean = true;
  showSignup:boolean = false;
  showForgot:boolean = false;


  constructor(private loginServise : LoginService) { }

  ngOnInit(): void {  }

  onLoginSubmit(){
    if(!this.email){
      alert("Please enter Email address");
      return;
    }
    const newLogin : LoginForm = {
      email: this.email,
      password: this.password
    }
    this.onLogin.emit(newLogin);
    this.email='';
    this.password='';
  }

  onSignupSubmit(){
    if(!this.name){
      alert("Please enter Name");
      return;
    }
    const newSignup : SignupForm = {
      name : this.name,
      birthdate : this.birthdate,
      gender : this.gender,
      interest : this.interest,
      email : this.email,
      password : this.password
    }
    this.onSignup.emit(newSignup);

    this.name='';
    this.birthdate='';
    this.interest='';
    this.email='';
    this.password='';
  }
  onForgotSubmit(){
    if(!this.email){
      alert("Please enter e-mail");
      return;
    }
    const newForgot : ForgotForm = {
      email : this.email
    }
    this.onForgot.emit(newForgot);
    alert("Recovery message sent to: " + this.email);
    this.email='';
    this.onClickLogin();
  }



  onClickSignup(){
    this.showLogin  = false;
    this.showForgot = false;
    this.showSignup = true;
  }
  onClickLogin(){
    this.showForgot = false;
    this.showSignup = false;
    this.showLogin  = true;
  }
  onClickForgot(){
    this.showLogin  = false;
    this.showSignup = false;
    this.showForgot = true;
  }
}
