import { Component, OnInit } from '@angular/core';
import { Observable, Subscription } from 'rxjs'
import {catchError, map} from 'rxjs/operators'
import {Router} from "@angular/router";
import {AuthenticationService, ChatService, UserService} from "src/app/services";

import { User, Message } from "src/app/models";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit{

  chatActiveWith : string;
  chatActiveWithId : number;
  displayChat: boolean = true;
  displayOnline : boolean = true;
  matches: User[];
  messageCurrent: string = "";


  constructor(private authenticationService: AuthenticationService,
              private router: Router,
              private chatService: ChatService) {
    this.matches = this.authenticationService.currentUserValue.matches;
    this.chatActiveWith = this.matches[0].name;
    this.chatActiveWithId = this.matches[0].id;
    console.log(this.matches);
    this.chatService.connect();
  }

  ngOnInit(): void {
    console.log(this.matches)
  }


  openChatWith(user: User) {
    this.chatActiveWith = user.name;
    this.chatActiveWithId = user.id;
    this.displayChat = true;
  }

  toHome() {
    this.router.navigateByUrl('/');
  }

  onSend() {
    this.messageCurrent = (document.getElementById("idMessageInput") as HTMLInputElement).value;
    if(this.messageCurrent == null || this.messageCurrent == '')
      return;

    var msg: Message;
    msg.id  = this.chatActiveWithId;
    msg.msg = this.messageCurrent;
    (document.getElementById("idMessageInput") as HTMLInputElement).value = "";

    this.chatService.send(this.messageCurrent);
    (document.getElementById("messagesList")as HTMLUListElement).innerHTML += "<li>" + msg.msg + "</li>";
  }
}
