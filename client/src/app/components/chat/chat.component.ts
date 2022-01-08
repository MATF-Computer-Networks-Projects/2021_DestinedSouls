import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Observable, Subject, Subscription} from 'rxjs'
import {catchError, map, tap} from 'rxjs/operators'
import {Router} from "@angular/router";
import {AuthenticationService, ChatService, UserService} from "src/app/services";

import {User, ChatMessage, Message, MatchUser} from "src/app/models";
import {WebsocketService} from "../../services/websocket.service";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit {

  chatActiveWith : BehaviorSubject<MatchUser>;

  displayChat: boolean = true;
  displayOnline : boolean = true;

  messagePending: Message[] = [];

  matches: MatchUser[];
  messages$: Subject<Message>;



  constructor(private authenticationService: AuthenticationService,
              private router: Router,
              private chatService: ChatService) {

    // TODO: if no matches offer to switch to matching

    this.matches = this.authenticationService.currentUserValue.matches;
    this.chatActiveWith = new BehaviorSubject<MatchUser>(this.matches[0]);
    this.displayChat = this.matches && this.matches.length > 0;


    this.messages$ = chatService.messages;
    this.messages$.subscribe(
      (msg: Message) => {
              console.log(msg);
            if(!msg.msg && msg.id) {
              if(this.messagePending.length > 0) {
                const idx = this.messagePending.findIndex(value => value.id === msg.id);
                this.matches.find(m => m.id === msg.id).messages.push({received: false, msg: this.messagePending[idx].msg});
                this.messagePending.splice(idx, 1);
              }
              return;
            }
            console.log(`Response from ${msg.id}: ${msg.msg}`);
            if(msg.id){
              this.matches.find(m => m.id === msg.id)
                      .messages.push({received: !msg.token, msg: msg.msg});
            }
          },
      error => {

      },
      () => { console.log("Closing...") }
      );


  }

  ngOnInit(): void {
    console.log(this.matches)
  }

  openChatWith(user: MatchUser) {
    this.chatActiveWith.next(user);
    this.displayChat = true;
  }

  toHome() {
    this.router.navigateByUrl('/');
  }

  toMatches() {
    this.router.navigateByUrl('/swipe');
  }

  onSend() {
    const messageCurrent = (document.getElementById("idMessageInput") as HTMLInputElement).value;
    if(messageCurrent == null || messageCurrent == '')
      return;

    (document.getElementById("idMessageInput") as HTMLInputElement).value = "";

    console.log(`new message from client to ${this.chatActiveWith.value.id}: ${messageCurrent}`);

    this.messages$.next({
      token: this.authenticationService.currentUserValue.token,
      id:    this.chatActiveWith.value.id,
      msg:   messageCurrent
    });
    this.messagePending.push({id: this.chatActiveWith.value.id, msg: messageCurrent});
  }
}
