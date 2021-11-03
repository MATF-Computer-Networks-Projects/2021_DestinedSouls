import { Component, OnInit ,Input} from '@angular/core';

@Component({
  selector: 'app-form-label',
  templateUrl: './form-label.component.html',
  styleUrls: ['./form-label.component.css']
})
export class FormLabelComponent implements OnInit {

  @Input() text : string | any;
  @Input() class : string | any;
  
  constructor() { }

  ngOnInit(): void {
  }

}
