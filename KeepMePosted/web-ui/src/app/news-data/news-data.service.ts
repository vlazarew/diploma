import {Injectable} from '@angular/core';
import {News} from "./news-data.component";
import {ApiService} from "../api/api.service";
import {TimePeriodService} from "../header/header.component";

@Injectable({
  providedIn: 'root'
})
export class NewsDataService {

  newsOnPage: number = 10;
  numberOfPage: number = 0;
  typeOfTimePeriod: string = "10min";

  news$: any;

  constructor(private apiService: ApiService) {
    setInterval(() => this.getNewsFromDB(this.typeOfTimePeriod), 10000);
  }

  getNewsFromDB(typeOfTimePeriod: string): any {
    this.typeOfTimePeriod = typeOfTimePeriod;
    this.apiService.get('/news?numberOfPage=' + this.numberOfPage +
      '&newsOnPage=' + this.newsOnPage +
      '&typeOfTimePeriod=' + typeOfTimePeriod).subscribe(data => {
      this.news$ = data
    })
  }

  getNews(): any {
    return this.news$;
  }

}
