import { Component, OnInit } from '@angular/core';

declare const $: any;
@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})

export class ChatComponent implements OnInit{

  activeUsers: string[] = ['Aleksa','Djordje','Petar'];

  constructor() {
  }

  ngOnInit() : void{
    $('.menu .item').tab();
  }


}
