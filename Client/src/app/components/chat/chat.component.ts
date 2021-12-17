import { Component, OnInit } from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent  {

  activeUsers: string[] = ['Aleksa','Djordje','Petar'];

  constructor() {
  }




}
