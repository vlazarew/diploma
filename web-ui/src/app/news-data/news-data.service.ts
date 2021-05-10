import {Injectable} from '@angular/core';
import {ApiService, urlDB} from '../api/api.service';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class NewsDataService {

  newsOnPage;
  numberOfPage;
  typeOfTimePeriod;

  news$: any;

  constructor(private apiService: ApiService,
              private httpClient: HttpClient) {
    this.newsOnPage = 10;
    this.numberOfPage = 0;
    this.typeOfTimePeriod = '10min';

    this.getNewsFromDB(this.typeOfTimePeriod);
    setInterval(() => this.getNewsFromDBByCountNews(this.newsOnPage, this.numberOfPage), 10000);
  }

  getNewsFromDB(typeOfTimePeriod: string): any {
    this.typeOfTimePeriod = typeOfTimePeriod;
    this.apiService.get('/news?numberOfPage=' + this.numberOfPage +
      '&newsOnPage=' + this.newsOnPage +
      '&typeOfTimePeriod=' + typeOfTimePeriod).subscribe(data => {
      this.news$ = data;
    });
  }

  getNewsFromDBByCountNews(newsOnPage: number, numberOfPage: number): any {
    this.newsOnPage = newsOnPage;
    this.numberOfPage = numberOfPage;

    this.apiService.get('/news?numberOfPage=' + numberOfPage +
      '&newsOnPage=' + newsOnPage +
      '&typeOfTimePeriod=' + this.typeOfTimePeriod).subscribe(data => {
      this.news$ = data;
    });
  }

  getNews(): any {
    return this.news$;
  }

  updateCountOfViews(id: number) {
    return this.httpClient.post(urlDB + '/news/add_count_of_views', id, {
      headers: new HttpHeaders().set('Content-type', 'application/json'),
    }).subscribe(this.getNewsFromDB(this.typeOfTimePeriod));
  }
}
