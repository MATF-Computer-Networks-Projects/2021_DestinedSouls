import { Injectable } from "@angular/core";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import {catchError, delay, filter, map, retryWhen, switchAll, switchMap, tap} from 'rxjs/operators';
import {EMPTY, Observable, of, Subject} from "rxjs";
import {Message} from "../models/message";
import {AuthenticationService} from "./authentication.service";


@Injectable()
export class ChatService {

  constructor(private authenticationService: AuthenticationService) {
  }
  private socket$: WebSocketSubject<any>
  RETRY_SECONDS = 10;
  private store: any;
  connect(): Observable<any> {
    return of('http://localhost:3000').pipe(
      filter(apiUrl => !!apiUrl),
      // https becomes wws, http becomes ws
      map(apiUrl => apiUrl.replace(/^http/, 'ws') + '/chat'),
      switchMap(wsUrl => {
        if (this.socket$) {
          return this.socket$;
        } else {
          this.socket$ = webSocket(wsUrl);
          return this.socket$;
        }
      }),
      retryWhen((errors) => errors.pipe(delay(this.RETRY_SECONDS)))
    );
  }

  send(data: any) {
    if (this.socket$) {
      const payload = {
        token: this.authenticationService.currentUserValue.token,
        ...data,
      };
      this.socket$.next(payload);
    }
  }


}
