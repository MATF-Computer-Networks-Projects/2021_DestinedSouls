import {Gender, Interest} from "./gender";

export interface User {
  id: number;  
  name?: string;
  birthday?: string;
  gender?: Gender;
  interest?: Interest;
}
