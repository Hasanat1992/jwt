import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StorageService } from '../_services/storage.service';

@Component({
  selector: 'app-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.css']
})
export class ViewComponent implements OnInit {
  constructor(private router: Router, private storageService: StorageService){}
  ngOnInit(): void {
    // if (this.storageService.isLoggedIn()) {
      
    // }else{
    //   this.router.navigateByUrl('/login')
    // }
  }

}
