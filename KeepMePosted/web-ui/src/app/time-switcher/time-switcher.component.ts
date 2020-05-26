import {Component, OnInit} from '@angular/core';
import {TimePeriodService} from '../header/header.component';
import {valueReferenceToExpression} from "@angular/compiler-cli/src/ngtsc/annotations/src/util";

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

  OnClick10min() {
    this.timePeriodService.typeOfTimePeriod.next('10min');
  }

  OnClick1h() {
    this.timePeriodService.typeOfTimePeriod.next('1h');
  }

  OnClick1d() {
    this.timePeriodService.typeOfTimePeriod.next('1d');
  }

  OnClick1w() {
    this.timePeriodService.typeOfTimePeriod.next('1w');
  }

  OnClick1m() {
    this.timePeriodService.typeOfTimePeriod.next('1m');
  }

  OnClick1y() {
    this.timePeriodService.typeOfTimePeriod.next('1y');
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
