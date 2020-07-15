import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../serviceApi/api.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent implements OnInit {
  private loggedType: string;
  constructor(private auth: ApiService, private route: Router) {

    
  }

  ngOnInit() {
    //console.log(this.auth.getAuthType());

  }
  logout() {
    //this.loggedType = "home";
    //this.auth.removeToken();
    //this.route.navigate(['/login']);
  }

}