export interface Message {
  token?: string;
  id?: number;
  msg?: string;
}

export interface ChatMessage {
  received: boolean;
  msg: string;
}

export interface ChatConversation {
  id: number;
  messages: ChatMessage[];
}
