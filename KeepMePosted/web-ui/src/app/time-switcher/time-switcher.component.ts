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

const LIST: string[] = ['10 минут', 'Час', 'День', 'Неделя', 'Месяц', 'Год'];

@Component({
  selector: 'app-time-switcher',
  templateUrl: './time-switcher.component.html',
  styleUrls: ['./time-switcher.component.css']
})
export class TimeSwitcherComponent implements OnInit {

  public list: string[] = LIST;
  public activeItem: string;

  constructor(private timePeriodService: TimePeriodService) {
    this.activeItem = this.list[0];
  }

  ngOnInit(): void {
  }

  OnSelectedItem(item: string): void {
    this.activeItem = item;
    switch (item) {
      case '10 минут': {
        this.timePeriodService.typeOfTimePeriod.next('10min');
        break;
      }
      case 'Час': {
        this.timePeriodService.typeOfTimePeriod.next('1h');
        break;
      }
      case 'День': {
        this.timePeriodService.typeOfTimePeriod.next('1d');
        break;
      }
      case 'Неделя': {
        this.timePeriodService.typeOfTimePeriod.next('1w');
        break;
      }
      case 'Месяц': {
        this.timePeriodService.typeOfTimePeriod.next('1m');
        break;
      }
      case 'Год': {
        this.timePeriodService.typeOfTimePeriod.next('1y');
        break;
      }
    }
  }



}
