import {Component, Injectable, OnInit} from '@angular/core';
import {Subject} from "rxjs";
import {NewsDataService} from "../news-data/news-data.service";

@Injectable({
  providedIn: 'root',
})
export class TimePeriodService {
  typeOfTimePeriod: Subject<string> = new Subject<string>();

  constructor(private newsDataService:NewsDataService) {
    this.typeOfTimePeriod.next("10min");
    this.typeOfTimePeriod.asObservable().subscribe((data) => {
      this.newsDataService.getNewsFromDB(data);
    })
  }
}

export interface UserInfo {
  firstname: string
  lastname: string
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  registered: boolean = true
  user: UserInfo = {firstname: "Vova", lastname: 'Lazarev'}

  constructor(private timePeriodService: TimePeriodService,
              private newsDataService: NewsDataService) {
  }

  ngOnInit(): void {
  }

  OnClick10min() {
    this.timePeriodService.typeOfTimePeriod.next("10min");
  }

  OnClick1h() {
    this.timePeriodService.typeOfTimePeriod.next("1h");
  }

  OnClick1d() {
    this.timePeriodService.typeOfTimePeriod.next("1d");
  }

  OnClick1w() {
    this.timePeriodService.typeOfTimePeriod.next("1w");
  }

  OnClick1m() {
    this.timePeriodService.typeOfTimePeriod.next("1m");
  }

  OnClick1y() {
    this.timePeriodService.typeOfTimePeriod.next("1y");
  }

}
