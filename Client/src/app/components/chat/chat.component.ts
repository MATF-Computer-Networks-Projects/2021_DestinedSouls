import { Component, OnInit } from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent  {

  onlineUsers: string[] = ['Aleksa','Djordje','Petar'];
  chatActiveWith : string;
  displayChat: boolean = false;
  constructor() {
  }


  openChatWith(user: string) {
    if(!this.onlineUsers.some(elem => elem ==user)){
      console.log("User: " + user + " not online, can't chat with him anymore :(");
    }
    this.chatActiveWith = user;
    this.displayChat = true;
  }
}
