import { Component, OnInit } from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';
import {AuthenticationService, UserService} from "../../services";
import {Router} from "@angular/router";
import { User } from 'src/app/models';
import {BehaviorSubject, Observable, Subject, Subscribable} from "rxjs";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit{

  onlineUsers: string[] = ['Aleksa','Djordje','Petar'];
  users : Subject<User[]>;
  usersObservable : Observable<User[]>;
  usersNames : String[];
  chatActiveWith : string;
  displayChat: boolean = false;
  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) { }

  ngOnInit(): void {
    this.userService.getOnline().subscribe(this.users);
    this.usersObservable = this.users.asObservable();
    this.users
      .subscribe(next => {
       next.map(user => {this.usersNames.push(user.name)});
    })

  }


  openChatWith(user: string) {
    if(!this.usersNames.some(elem => elem == user)){
      console.log("User: " + user + "is not online, can't chat with him anymore :(");
    }
    this.chatActiveWith = user;
    this.displayChat = true;
  }
}
