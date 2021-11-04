import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { LoginForm } from './Form';
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
  /*onSubmit --> onLogin*/
  email:string | any;
  password:string | any;
  
  constructor(private loginServise : LoginService) { }

  ngOnInit(): void {  }

  onSubmit(){
    if(!this.email){
      alert("Please enter Email address");
      return;
    }
    const newLogin : LoginForm = {
      email: this.email,
      password: this.password
    }

    //TODO send http POST
    this.onLogin.emit(newLogin);

    this.email='';
    this.password='';
    
  }

}
