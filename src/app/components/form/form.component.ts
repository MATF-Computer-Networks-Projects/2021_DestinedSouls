import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Form } from './Form';
import { Observable, throwError } from 'rxjs';
import { catchError, retry} from 'rxjs/operators';
import { LoginService } from 'src/app/services/login.service';

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {
  @Output() onLogin : EventEmitter<Form> = new EventEmitter();

  email:string | any;
  password:string | any;
  
  constructor(private loginServise : LoginService) { }

  ngOnInit(): void {  }
  onSubmit(){
    if(!this.email){
      alert("Please enter Email address");
      return;
    }
    const newForm = {
      email: this.email,
      password: this.password
    }
    
    //TODO send http POST
    //this.onLogin.emit(newForm);

    this.email='';
    this.password='';
    
  }

}
