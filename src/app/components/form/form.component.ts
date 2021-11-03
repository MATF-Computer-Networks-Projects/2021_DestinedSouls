import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {

  email:string | any;
  password:string | any;
  
  constructor() { }
  ngOnInit(): void {  }
  onSubmit(){
    if(!this.email)
      alert("Please enter Email address");
    

    /*TODO emit*/
    this.email='';
    this.password='';
    
    
    return;
  }

}
