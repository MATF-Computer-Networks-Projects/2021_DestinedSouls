import { Component, OnInit } from '@angular/core';
import {MatTabsModule} from '@angular/material/tabs';
import {AuthenticationService, UserService} from "../../services";
import {Router} from "@angular/router";
import { User } from 'src/app/models';
import {BehaviorSubject, Observable, Observer, Subject, Subscribable} from "rxjs";
import {map} from "rxjs/operators";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit{

  onlineUsers: string[] = ['Aleksa','Djordje','Petar'];
  usersArray : User[];
  usersNames : string[] = [];
  chatActiveWith : string;
  displayChat: boolean = false;
  displayOnline : boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private userService: UserService,
              private router: Router) { }

  ngOnInit(): void {

  }


  openChatWith(user: string) {
    if(!this.usersNames.includes(user)){
      console.log("User: " + user + "is not online, can't chat with him anymore :(");
    }
    this.chatActiveWith = user;
    this.displayChat = true;
  }


  addOnline() {
    this.userService.getOnline()
      .subscribe( value  => {
        this.usersArray = value;
        this.usersArray.map( user => {
          if(!this.usersNames.includes(user.name))
              this.usersNames.push(user.name); });
        console.log("neko se ulogovao!");
        this.displayOnline=true;
      });
  }
}
