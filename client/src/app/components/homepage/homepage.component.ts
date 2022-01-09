import {Component, OnDestroy, OnInit} from '@angular/core';
import { Router } from '@angular/router'
import {AuthenticationService, ChatService, UserService} from 'src/app/services'
import {LoggedUser, MatchUser, Message, User} from "../../models";

@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit, OnDestroy {
  public user: LoggedUser;
  public path: string;

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private chatService: ChatService,
              private router: Router) {
    this.chatService.messages.subscribe(
      (msg: Message) => {
        if(!msg)
          return;
        console.log(`Response from ${msg.id}: ${msg.msg}`);
        if (msg.id) {
          if(msg.msg)
            this.authenticationService.currentUserValue.matches.find(m => m.id === msg.id)
              .messages.push({received: !msg.token, msg: msg.msg});
        }
        else if(msg.token) {

          msg.token = msg.token.split("'").join('"');


          console.log("New match!\n" + msg.token);
          let user = (JSON.parse(msg.token) as MatchUser);
          user.messages = [];
          this.authenticationService.currentUserValue.matches.push(user);
        }

      }
    );
  }

  ngOnInit(): void {
    this.user = this.authenticationService.currentUserValue;
    this.path = 'upload/profile/' + this.user.image;
  }

  ngOnDestroy() {
    this.chatService.messages.complete();
  }

  onLogout() {
    this.authenticationService.logout();
    this.router.navigateByUrl('/login')
      .then(() => {
        window.location.reload();
      });
  }

  onChat() {
    this.router.navigateByUrl('/chat');
  }

  onSwipe() {
    this.router.navigateByUrl('/swipe');
  }
}
