import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export const urlDB = 'http://localhost:8080/';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private httpClient: HttpClient) {
  }

  get(path: string) {
    return this.httpClient.get(urlDB + path);
  }

  // post(path: string) {
  //   return this.httpClient.post(urlDB + path, );
  // }
}
