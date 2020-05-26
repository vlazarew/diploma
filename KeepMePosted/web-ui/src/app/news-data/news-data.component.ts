import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {ApiService} from "../api/api.service";
import {TimePeriodService} from "../header/header.component";
import {NewsDataService} from "./news-data.service";

export interface News {
  position: number;
  delta: number;
  logo: string;
  title: string;
  countOfViews: number;
}

@Component({
  selector: 'app-news-data',
  templateUrl: './news-data.component.html',
  styleUrls: ['./news-data.component.css']
})
export class NewsDataComponent implements OnInit {

  allNews: any;

  newsItems: News[] = [
    {position: 1, delta: 1, logo: 'assets/izvestiya_logo.jpg', title: "news 1", countOfViews: 15},
    {position: 2, delta: -2, logo: 'assets/lentaRu_logo.png', title: "news 2", countOfViews: 1},
    {position: 3, delta: 3, logo: 'assets/rbk_logo.jpg', title: "news 3", countOfViews: 2},
    {position: 4, delta: -4, logo: 'assets/riaTass_logo.png', title: "news 4", countOfViews: 3},
    {position: 5, delta: 5, logo: 'assets/vedomosti_logo.png', title: "news 5", countOfViews: 5},
  ]

  displayedColumns: string[] = ['position', 'delta', 'logo', 'title', 'countOfViews'];

  constructor(private httpClient: HttpClient,
              private router: Router,
              private apiService: ApiService,
              private newsDataService: NewsDataService) {
  }

  ngOnInit(): void {
    setInterval(()=>this.setAllNews(), 10);
  }

  setAllNews(){
    this.allNews = this.newsDataService.getNews();
  }

}
