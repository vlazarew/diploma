import {Component, OnInit} from '@angular/core';

export interface UserInfo {
  firstname: string;
  lastname: string;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  registered = true;
  user: UserInfo = {firstname: 'Vova', lastname: 'Lazarev'};

  constructor() {
  }

  ngOnInit(): void {
  }
}
