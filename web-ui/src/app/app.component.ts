import {Component, OnInit} from '@angular/core';



export interface geoPosition {
  longitude: number;
  latitude: number;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  currentGeoPosition: geoPosition = {longitude: 0, latitude: 0}
  title: "temp title";


  ngOnInit(): void {
    this.takeGeoPosition()
    setTimeout(() => {
      var lat = document.getElementById("lat");
      var lon = document.getElementById("lon");

      this.currentGeoPosition.latitude = Number(lat.innerHTML);
      this.currentGeoPosition.longitude = Number(lon.innerHTML);
    })
  }

  takeGeoPosition(): void {
    var lat = document.getElementById("lat");
    var lon = document.getElementById("lon");
    navigator.geolocation.getCurrentPosition(function (position) {
        lat.innerHTML = position.coords.latitude.toString();
        lon.innerHTML = position.coords.longitude.toString();
      }
    )
  }


}
