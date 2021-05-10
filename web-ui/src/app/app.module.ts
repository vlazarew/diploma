import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {HeaderComponent} from './header/header.component';
import {AppRoutingModule} from './app-routing.module';
import {RouterModule} from '@angular/router';
import {HomeComponent} from './home/home.component';
import {FooterComponent} from './footer/footer.component';
import {NewsDataComponent} from './news-data/news-data.component';
import {MatTableModule} from '@angular/material/table';
import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatIconModule} from '@angular/material/icon';
import {MatPaginatorIntl, MatPaginatorModule} from '@angular/material/paginator';
import {HttpClientModule} from '@angular/common/http';
import {NewsDataService} from './news-data/news-data.service';
import {TimeSwitcherComponent} from './time-switcher/time-switcher.component';
import {CustomPaginator} from './custom-paginator/custom-paginator';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HomeComponent,
    FooterComponent,
    NewsDataComponent,
    TimeSwitcherComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule,
    BrowserAnimationsModule,
    MatTableModule,
    MatMenuModule,
    MatButtonModule,
    MatToolbarModule,
    MatButtonToggleModule,
    MatIconModule,
    MatPaginatorModule,
    HttpClientModule
  ],
  providers: [NewsDataService,
    {provide: MatPaginatorIntl, useClass: CustomPaginator}],
  bootstrap: [AppComponent]
})
export class AppModule {
}
