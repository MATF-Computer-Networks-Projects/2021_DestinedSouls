import { Component, OnInit,Input } from '@angular/core';

@Component({
  selector: 'app-form-btn',
  templateUrl: './form-btn.component.html',
  styleUrls: ['./form-btn.component.css']
})
export class FormBtnComponent implements OnInit {
  @Input() class : string | any;
  @Input() color : string | any;
  @Input() text : string | any;
  
  constructor() { }

  ngOnInit(): void {
  }

}
