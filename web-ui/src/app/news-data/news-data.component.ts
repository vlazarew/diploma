import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {ApiService} from '../api/api.service';
import {NewsDataService} from './news-data.service';
import {PageEvent} from '@angular/material/paginator';

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
  countOfNews: number;
  countOfPages: number;

  pageSize: number;
  pageIndex: number;

  displayedColumns: string[] = ['position', 'delta', 'logo', 'title', 'countOfViewers'];
  pageEvent: PageEvent;

  constructor(private httpClient: HttpClient,
              private router: Router,
              private apiService: ApiService,
              private newsDataService: NewsDataService) {
    this.pageSize = 10;
    this.pageIndex = 0;
  }

  ngOnInit(): void {
    setInterval(() => this.setAllNews(), 10);
  }

  setAllNews() {
    const data = this.newsDataService.getNews();
    if (data === undefined) {
      return;
    }

    if (data[0].length === 0) {
      this.pageIndex = 0;
      this.newsDataService.getNewsFromDBByCountNews(this.pageSize, this.pageIndex);
      this.setAllNews();
    }
    this.allNews = data[0];
    this.countOfNews = data[1];
    this.countOfPages = data[2];
  }

  OnClickNews(element: any): void {
    this.newsDataService.updateCountOfViews(element.id);
  }

  UpdateCount(event: any, element: any): void {
    if (event.button === 1) {
      this.newsDataService.updateCountOfViews(element.id);
    }
  }

  setPageSizeOptions(setPageSizeOptionsInput: any): void {
    this.pageSize = setPageSizeOptionsInput.pageSize;
    this.pageIndex = setPageSizeOptionsInput.pageIndex;

    this.newsDataService.getNewsFromDBByCountNews(this.pageSize, this.pageIndex);
    this.setAllNews();
    // if (this.allNews[0].length === 0) {
    //   this.pageIndex = 0;
    //   this.newsDataService.getNewsFromDBByCountNews(this.pageSize, this.pageIndex);
    //   this.setAllNews();
    // }
  }
}
