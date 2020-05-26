import {Injectable} from '@angular/core';
import {ApiService} from '../api/api.service';

@Injectable({
  providedIn: 'root'
})
export class NewsDataService {

  newsOnPage = 10;
  numberOfPage = 0;
  typeOfTimePeriod = '10min';

  news$: any;

  constructor(private apiService: ApiService) {
    this.getNewsFromDB(this.typeOfTimePeriod);
    setInterval(() => this.getNewsFromDB(this.typeOfTimePeriod), 10000);
  }

  getNewsFromDB(typeOfTimePeriod: string): any {
    this.typeOfTimePeriod = typeOfTimePeriod;
    this.apiService.get('/news?numberOfPage=' + this.numberOfPage +
      '&newsOnPage=' + this.newsOnPage +
      '&typeOfTimePeriod=' + typeOfTimePeriod).subscribe(data => {
      this.news$ = data;
    });
  }

  getNews(): any {
    return this.news$;
  }

}
