import {Component, Injectable, OnInit} from '@angular/core';
import {Subject} from 'rxjs';
import {NewsDataService} from '../news-data/news-data.service';

@Injectable({
  providedIn: 'root',
})
export class TimePeriodService {
  typeOfTimePeriod: Subject<string> = new Subject<string>();

  constructor(private newsDataService: NewsDataService) {
    this.typeOfTimePeriod.next('10min');
    this.typeOfTimePeriod.asObservable().subscribe((data) => {
      this.newsDataService.getNewsFromDB(data);
    });
  }
}

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
