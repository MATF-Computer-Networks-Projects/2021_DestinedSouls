import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-form-input',
  templateUrl: './form-input.component.html',
  styleUrls: ['./form-input.component.css']
})
export class FormInputComponent implements OnInit {
  @Input() name: string | any;
  @Input() type: string | any;
  @Input() placeholder: string | any;
    
  constructor(){}

  ngOnInit(): void {}

}
