import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewsDataComponent } from './news-data.component';

describe('NewsDataComponent', () => {
  let component: NewsDataComponent;
  let fixture: ComponentFixture<NewsDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewsDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewsDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
